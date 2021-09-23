package tech.watanave.fulla

import tech.watanave.fulla.annotation.State
import tech.watanave.fulla.samplestate.Action
import tech.watanave.fulla.samplestate.reducer

@State
data class SampleState(
    val name: String,
    val age: Int,
    val map: Map<String, Int>
)

class ViewModel {

    var currentState = SampleState(
        name = "watanave",
        age = 35,
        hashMapOf("key" to 0)
    )

    fun post(action: Action) {
        currentState = reducer(currentState, action)
    }
}
