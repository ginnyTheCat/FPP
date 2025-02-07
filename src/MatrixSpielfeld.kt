import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

val colors = listOf(
    Color.Red,
    Color.Blue,
    Color.Yellow,
    Color.Green,
)

class MatrixSpielfeld(hoehe: Int, breite: Int, onSet: (x: Int, y: Int, spieler: Spieler) -> Unit) :
    Spielfeld(breite, hoehe, onSet) {

    private val matrix = mutableStateListOf<Spieler?>().also {
        it.addAll(List(hoehe * breite) { null })
    }

    override fun get(x: Int, y: Int) = matrix[y * breite + x]
    override fun set(x: Int, y: Int, spieler: Spieler) {
        super.set(x, y, spieler)
        matrix[y * breite + x] = spieler
    }

    @Composable
    override fun ui(onSet: (x: Int, y: Int) -> Unit) {
        val size = 400.dp
        val longSide = max(breite, hoehe)
        val squareSize = size / longSide

        LazyVerticalGrid(
            columns = GridCells.Fixed(breite),
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(4.dp)
                .size(size)
        ) {
            itemsIndexed(matrix) { i, entry ->
                val color = if (entry == null) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    colors[entry.name.hashCode().mod(colors.size)]
                }
                Box(
                    Modifier
                        .size(squareSize)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .clickable { onSet(i % breite, i / breite) },
                )
            }
        }
    }
}