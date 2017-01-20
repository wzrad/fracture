package dev.wizrad.fracture.game.world.components.contact

enum class ContactType(val value: Int) {
  Hero(1 shl 0),
  Wall(1 shl 1),
  Ceiling(1 shl 2),
  Ground(1 shl 3);

  // MARK: Representations
  val bits: Short get() = value.toShort()
}
