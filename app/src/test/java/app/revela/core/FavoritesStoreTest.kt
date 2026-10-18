package app.revela.core

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoritesStoreTest {

    private fun tmpDir(): File = createTempDirectory("revela-fav").toFile()

    @Test
    fun `sin fichero devuelve el default`() {
        assertEquals(FavoritesStore.DEFAULT, FavoritesStore.load(tmpDir()))
    }

    @Test
    fun `fichero corrupto devuelve el default sin lanzar`() {
        val dir = tmpDir()
        File(dir, "revela_favoritos.json").writeText("{no json")
        assertEquals(FavoritesStore.DEFAULT, FavoritesStore.load(dir))
    }

    @Test
    fun `guardar y recargar conserva el orden`() {
        val dir = tmpDir()
        val ids = listOf("crop", "tune", "doubleExpo", "text")
        FavoritesStore.save(dir, ids)
        assertEquals(ids, FavoritesStore.load(dir))
    }

    @Test
    fun `quitar un favorito persiste el resto`() {
        val dir = tmpDir()
        val ids = listOf("tune", "crop", "heal")
        FavoritesStore.save(dir, ids)
        val rest = FavoritesStore.load(dir) - "crop"
        FavoritesStore.save(dir, rest)
        assertEquals(listOf("tune", "heal"), FavoritesStore.load(dir))
    }

    @Test
    fun `lista vacia persiste vacia`() {
        val dir = tmpDir()
        FavoritesStore.save(dir, emptyList())
        assertEquals(emptyList(), FavoritesStore.load(dir))
    }
}
