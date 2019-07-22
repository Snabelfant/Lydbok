package dag.lydbok.model

typealias Lydbøker = List<Lydbok>

fun Lydbøker.getSelected() = this.find { it.isSelected }!!

