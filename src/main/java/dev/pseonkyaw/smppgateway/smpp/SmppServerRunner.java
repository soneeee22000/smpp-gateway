package dev.pseonkyaw.smppgateway.smpp;

import dev.pseonkyaw.smppgateway.config.SmppProperties;
import dev.pseonkyaw.smppgateway.mq.InboundSmsPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.InterfaceVersion;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.jsmpp.session.SessionStateListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmppServerRunner implements SessionStateListener {

    private final SmppProperties props;
    private final InboundSmsPublisher publisher;

    private SMPPServerSessionListener sessionListener;
    private ExecutorService acceptorPool;
    private ExecutorService sessionPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @PostConstruct
    public void start() throws IOException {
        sessionListener = new SMPPServerSessionListener(props.port());
        sessionListener.setPduProcessorDegree(props.processorDegree());

        acceptorPool = Executors.newSingleThreadExecutor();
        sessionPool = Executors.newCachedThreadPool();
        running.set(true);

        acceptorPool.submit(this::acceptLoop);
        log.info("SMPP server listening on port={}", props.port());
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                SMPPServerSession session = sessionListener.accept();
                log.info("accepted SMPP session sessionId={}", session.getSessionId());
                session.addSessionStateListener(this);
                sessionPool.submit(() -> bindAndServe(session));
            } catch (IOException e) {
                if (running.get()) {
                    log.warn("accept loop IO error", e);
                }
            }
        }
    }

    private void bindAndServe(SMPPServerSession session) {
        try {
            BindRequest bindReq = session.waitForBind(props.bindTimeoutMs());
            if (!props.systemId().equals(bindReq.getSystemId())
                    || !props.password().equals(bindReq.getPassword())) {
                log.warn("rejected bind systemId={}", bindReq.getSystemId());
                bindReq.reject(0x0D);
                return;
            }

            BindType bindType = bindReq.getBindType();
            if (bindType != BindType.BIND_TRX && bindType != BindType.BIND_TX && bindType != BindType.BIND_RX) {
                bindReq.reject(0x04);
                return;
            }

            session.setMessageReceiverListener(new GatewayMessageReceiver(bindReq.getSystemId(), publisher));
            bindReq.accept("gateway", InterfaceVersion.IF_34);
            log.info("bound session systemId={} type={}", bindReq.getSystemId(), bindType);
        } catch (Exception e) {
            log.error("error servicing SMPP session", e);
            session.close();
        }
    }

    @Override
    public void onStateChange(SessionState newState, SessionState oldState, org.jsmpp.session.Session source) {
        log.debug("session state {} -> {}", oldState, newState);
    }

    @PreDestroy
    public void stop() throws IOException {
        running.set(false);
        if (sessionListener != null) sessionListener.close();
        if (acceptorPool != null) acceptorPool.shutdownNow();
        if (sessionPool != null) sessionPool.shutdownNow();
        log.info("SMPP server stopped");
    }
}
