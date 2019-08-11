package dag.lydbok.model

typealias Lydbøker = List<Lydbok>

var Lydbøker.selected
    get() = this.find { it.isSelected }!!
    set(lydbok) {
        selected.isSelected = false
        lydbok.isSelected = true
    }

val Lydbøker.selectedIndex get() = this.indexOfFirst { it.isSelected }

