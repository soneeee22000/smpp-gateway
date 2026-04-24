# Changelog

All notable changes to this project follow [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Portfolio-grade README with mermaid architecture, SMPP protocol sequence diagram, and tech-stack mindmap
- MIT license
- Design-decisions table covering jsmpp library choice, non-SUBMIT PDU rejection strategy, topic-exchange routing, graceful shutdown, and the `ServerMessageReceiverListener` vs `MessageReceiverListener` distinction

## [0.1.0] — 2026-04-24

### Added

- Initial Spring Boot 3.5 scaffold (Java 21)
- `smpp` package: `SmppServerRunner` (acceptor loop + per-session executor pool), `GatewayMessageReceiver` (full `ServerMessageReceiverListener` implementation)
- `mq` package: `InboundSms` record, `RabbitConfig` (topic exchange + durable queue + wildcard binding), `InboundSmsPublisher`
- `config` package: `SmppProperties` with sensible defaults (port 2775, degree 4, 15 s bind timeout)
- Docker Compose with RabbitMQ 3.13 management image
- `submit_sm` → RabbitMQ publish path with `ESME_RSYSERR` fallback on MQ failure
- Graceful shutdown via `@PreDestroy`
