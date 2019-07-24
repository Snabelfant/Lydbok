package dag.lydbok.model

typealias Lydbøker = List<Lydbok>

fun Lydbøker.getSelected() = this.find { it.isSelected }!!
fun Lydbøker.getSelectedIndex() = this.indexOfFirst { it.isSelected }
fun Lydbøker.setSelected(lydbok: Lydbok) {
    getSelected().isSelected = false
    lydbok.isSelected = true
}

