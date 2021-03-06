package dev.wizrad.fracture.game.renderer.hero

import com.badlogic.gdx.graphics.Color
import dev.wizrad.fracture.game.renderer.core.Renderer
import dev.wizrad.fracture.game.renderer.support.pause
import dev.wizrad.fracture.game.world.hero.Hero
import dev.wizrad.fracture.game.world.hero.forms.SpaceJumpForm

fun Renderer.render(hero: Hero, form: SpaceJumpForm, delta: Float) {
  batch.pause {
    drawRect(hero, Color.CHARTREUSE)
  }
}