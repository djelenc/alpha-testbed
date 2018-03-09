package atb.infrastructure

/** States of an evaluation run */
sealed class EvaluationState

/** Evaluation has completed successfully */
data class Completed(val data: EvaluationData) : EvaluationState()

/** Evaluation has been interrupted */
data class Interrupted(val tick: Int, val data: EvaluationData) : EvaluationState()

/** Evaluation has ended with an exception  */
data class Faulted(val thrown: Throwable) : EvaluationState()

/** Evaluation has not yet started */
object Idle : EvaluationState()

/** Evaluation is running */
object Running : EvaluationState()