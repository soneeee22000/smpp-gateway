# smpp-gateway

Java SMPP server that accepts `submit_sm` PDUs, publishes each inbound SMS to **RabbitMQ**, and returns a generated `message_id` to the ESME. A minimal but real implementation of the protocol every telco uses to send and receive SMS (SMPP — Short Message Peer-to-Peer).

**Stack:** Java 21 · Spring Boot 3.5 · jsmpp 3.0 · RabbitMQ · Docker

## What is SMPP?

SMPP is the protocol that sits between **ESMEs** (External Short Messaging Entities — apps that send SMS) and **SMSCs** (Short Message Service Centers — telco infrastructure that actually delivers SMS). When your bank sends you an OTP, there is almost certainly an SMPP session somewhere in the chain. It is a binary protocol over TCP, not HTTP.

TRANSATEL (an MVNO aggregator) operates on top of this world — they receive SMPP-format messages from MVNOs and route them to the right MNOs, and they receive delivery receipts going the other way. This gateway demonstrates the **ESME-facing** side of that bridge.

## What this service does

```
 ┌────────────┐  submit_sm   ┌────────────────┐   publish   ┌──────────────┐
 │   ESME     │─────────────▶│  smpp-gateway  │────────────▶│   RabbitMQ   │
 │ (SMS app)  │◀─── resp ────│  (port 2775)   │             │ sms.inbound  │
 └────────────┘              └────────────────┘             └──────────────┘
```

1. Listens on TCP port `2775` for SMPP binds.
2. Authenticates the ESME (simple `system_id` + `password` check — swap for a real credential store in production).
3. Accepts `submit_sm` PDUs, generates a `message_id`, and publishes an `InboundSms` record to the `sms.inbound` RabbitMQ topic exchange.
4. Returns `submit_sm_resp` with the `message_id`.
5. Downstream workers can subscribe to the exchange for delivery, audit, rating, or fraud checks.

## Run it

```bash
docker compose up -d
./mvnw spring-boot:run
```

Then use any SMPP client (smpplib, a Python script, or the excellent `smppsim` / `smpp-cli`) to bind to `localhost:2775` with `system_id=gateway`, `password=gateway`, and submit an `SMS`.

## Project layout

```
dev.pseonkyaw.smppgateway
├── config/       SmppProperties (@ConfigurationProperties)
├── smpp/         SmppServerRunner + GatewayMessageReceiver (jsmpp glue)
└── mq/           RabbitConfig + InboundSmsPublisher + InboundSms DTO
```

## Status

Portfolio project. Not production. TLS, delivery receipts, throttling, rate-limiting per ESME, and proper credential storage are out of scope. Built to demonstrate familiarity with the **SMPP protocol** — an explicit nice-to-have on TRANSATEL's Java back-end JD — and Spring Boot idiomatic composition.
