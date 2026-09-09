# Part 2: Architecture and Trade-offs Requirements

The user supplied the following assessment text in an image on 09 September 2026, after the original exercise copy was prepared. This supplement preserves that additional source. The reference note in [the original statement](exercise-statement.md#part-2-architecture--tradeoffs-document) describes what was available when that copy was made; the original statement remains unchanged.

> Write a concise document covering the architectural decisions, trade-offs, and production considerations arising directly from your ledger implementation.
>
> Required sections:
>
> * Append-only at scale. What breaks first at 100× volume? Where does your design accumulate unbounded state, and what is the cheapest structural change that defers that problem?
> * Value-dated entries in production. Describe the operational and regulatory surface that value-dated entries create in a UAE-licensed bank, and name one control you would add before going live.
> * Authorization lifecycle. State every way an authorization in your model can end other than a matching settlement. For each, the real-world scenario it represents and the system behavior you would mandate.
> * What you cut and why. Every simplification made to stay in scope, and the production risk each one defers.
>
> Do not restate the event stream or reproduce rule text from Part 1.

The repository delivers [architecture](../architecture.md), [tradeoffs](../trade-offs.md) and [production considerations](../production-considerations.md), combined in [one PDF](../deliverables/architecture-tradeoffs-production.pdf). The user additionally requested a maximum of four PDF pages. This supplement supplies document requirements, not authorization to implement the production recommendations.
