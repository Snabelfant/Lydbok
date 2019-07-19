package dag.lydbok.model

typealias Points = List<Point>

fun Points.nearestBefore(time: Int) = this.findLast { it.lydbokOffset < time }

fun Points.nearestAfter(time: Int) = this.find { it.lydbokOffset > time }
