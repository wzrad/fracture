package dev.wizrad.fracture.game.renderer.level

import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.hero.render
import dev.wizrad.fracture.game.world.level.Level

fun Renderer.render(level: Level, delta: Float) {
  render(level.platforms, delta)
  render(level.hero, delta)
}
