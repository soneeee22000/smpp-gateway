package dev.pseonkyaw.smppgateway.smpp;

import dev.pseonkyaw.smppgateway.mq.InboundSms;
import dev.pseonkyaw.smppgateway.mq.InboundSmsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.BroadcastSm;
import org.jsmpp.bean.CancelBroadcastSm;
import org.jsmpp.bean.CancelSm;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.QueryBroadcastSm;
import org.jsmpp.bean.QuerySm;
import org.jsmpp.bean.ReplaceSm;
import org.jsmpp.bean.SubmitMulti;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.BroadcastSmResult;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.QueryBroadcastSmResult;
import org.jsmpp.session.QuerySmResult;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.session.SubmitMultiResult;
import org.jsmpp.session.SubmitSmResult;
import org.jsmpp.util.MessageId;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class GatewayMessageReceiver implements ServerMessageReceiverListener {

    private static final int ESME_RSYSERR = 0x08;

    private final String systemId;
    private final InboundSmsPublisher publisher;

    @Override
    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession source)
            throws ProcessRequestException {
        String id = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String text = new String(submitSm.getShortMessage(), StandardCharsets.UTF_8);

        InboundSms sms = new InboundSms(
                id,
                submitSm.getSourceAddr(),
                submitSm.getDestAddress(),
                text,
                systemId,
                Instant.now()
        );

        try {
            publisher.publish(sms);
            return new SubmitSmResult(new MessageId(id), new OptionalParameter[0]);
        } catch (RuntimeException ex) {
            log.error("failed to publish sms to rabbitmq", ex);
            throw new ProcessRequestException("internal error", ESME_RSYSERR);
        } catch (org.jsmpp.PDUStringException ex) {
            log.error("invalid message id", ex);
            throw new ProcessRequestException("invalid message id", ESME_RSYSERR);
        }
    }

    @Override
    public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti submitMulti, SMPPServerSession source)
            throws ProcessRequestException {
        throw new ProcessRequestException("submit_multi not supported", ESME_RSYSERR);
    }

    @Override
    public QuerySmResult onAcceptQuerySm(QuerySm querySm, SMPPServerSession source)
            throws ProcessRequestException {
        throw new ProcessRequestException("query_sm not supported", ESME_RSYSERR);
    }

    @Override
    public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession source)
            throws ProcessRequestException {
        throw new ProcessRequestException("replace_sm not supported", ESME_RSYSERR);
    }

    @Override
    public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession source)
            throws ProcessRequestException {
        throw new ProcessRequestException("cancel_sm not supported", ESME_RSYSERR);
    }

    @Override
    public BroadcastSmResult onAcceptBroadcastSm(BroadcastSm broadcastSm, SMPPServerSession source)
            throws ProcessRequestException {
        throw new ProcessRequestException("broadcast_sm not supported", ESME_RSYSERR);
    }

    @Override
    public void onAcceptCancelBroadcastSm(CancelBroadcastSm cancelBroadcastSm, SMPPServerSession source)
            throws ProcessRequestException {
        throw new ProcessRequestException("cancel_broadcast_sm not supported", ESME_RSYSERR);
    }

    @Override
    public QueryBroadcastSmResult onAcceptQueryBroadcastSm(QueryBroadcastSm queryBroadcastSm, SMPPServerSession source)
            throws ProcessRequestException {
        throw new ProcessRequestException("query_broadcast_sm not supported", ESME_RSYSERR);
    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
        throw new ProcessRequestException("data_sm not supported", ESME_RSYSERR);
    }
}
