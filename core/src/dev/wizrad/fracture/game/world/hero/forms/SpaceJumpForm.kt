package dev.wizrad.fracture.game.world.hero.forms

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import dev.wizrad.fracture.game.world.components.contact.ContactInfo.Orientation
import dev.wizrad.fracture.game.world.components.contact.ContactType
import dev.wizrad.fracture.game.world.components.statemachine.State
import dev.wizrad.fracture.game.world.core.Context
import dev.wizrad.fracture.support.Tag
import dev.wizrad.fracture.support.debug

class SpaceJumpForm(context: Context): Form(context) {
  // MARK: Form
  override fun initialState(): State {
    return Standing(context)
  }

  override fun defineFixtures(size: Vector2) {
    // create fixtures
    val width = size.x / 2
    val height = size.y / 2
    val square = PolygonShape()
    square.setAsBox(width, height, Vector2(width, height), 0.0f)

    val fixture = FixtureDef()
    fixture.shape = square
    fixture.density = 1.0f
    fixture.friction = 0.2f
    fixture.filter.categoryBits = ContactType.Hero.bits

    body.createFixture(fixture)

    // dispose shapes
    square.dispose()
  }

  // MARK: Direction
  enum class Direction { None, Left, Right }

  // MARK: States
  class Standing(context: Context): FormState(context) {
    private val runMagnitude = 7.5f

    override fun update(delta: Float) {
      super.update(delta)

      // apply running movement
      val force = Vector2()
      if (controls.left.isPressed) {
        force.x -= runMagnitude
      }

      if (controls.right.isPressed) {
        force.x += runMagnitude
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      if (controls.jump.isPressedUnique && canJump()) {
        return Windup(context)
      }

      return null
    }

    private fun canJump(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return contact.oriented(body.fixtureList.first(), Orientation.Bottom)
    }
  }

  class Windup(context: Context): FormState(context) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart(context, isShort = !controls.jump.isPressed)
      }

      return null
    }
  }

  class JumpStart(context: Context, isShort: Boolean): FormState(context) {
    private val magnitude = if (isShort) 2.5f else 5.0f

    override fun start() {
      debug(Tag.World, "$this applying impulse: $magnitude")
      val center = body.worldCenter
      body.applyLinearImpulse(0.0f, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      val frameLength = 3
      return if (frame >= frameLength) Jumping(context) else null
    }
  }

  class Jumping(context: Context): FormState(context) {
    private val driftMagnitude = 10.0f
    private var canJump: Boolean = false

    override fun update(delta: Float) {
      super.update(delta)

      if (!canJump && isFalling()) {
        canJump = true
        controls.jump.requireUniquePress()
      }

      // apply directional influence
      val force = Vector2()
      if (controls.left.isPressed) {
        force.x -= driftMagnitude
      }

      if (controls.right.isPressed) {
        force.x += driftMagnitude
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      if (didLand()) {
        return Landing(context)
      } else if (controls.jump.isPressedUnique && canJump) {
        return Windup2(context)
      }

      return null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return contact.oriented(body.fixtureList.first(), Orientation.Bottom)
    }

    private fun isFalling(): Boolean {
      return body.linearVelocity.y >= 0.0
    }
  }

  class Windup2(context: Context): FormState(context) {
    private val frameLength = 4

    override fun nextState(): State? {
      if (frame >= frameLength) {
        return JumpStart2(context, isShort = !controls.jump.isPressed, direction = inputDirection())
      }

      return null
    }

    private fun inputDirection(): Direction {
      val leftPressed = controls.left.isPressed
      val rightPressed = controls.right.isPressed

      return when {
        leftPressed && !rightPressed -> Direction.Left
        !leftPressed && rightPressed -> Direction.Right
        else -> Direction.None
      }
    }
  }

  class JumpStart2(context: Context, direction: Direction, isShort: Boolean): FormState(context) {
    private val direction = direction
    private val frameLength = 3
    private val magnitude = if (isShort) 5.0f else 7.5f

    override fun start() {
      // cancel vertical momentum
      val velocity = body.linearVelocity
      velocity.y = 0.0f

      // cancel horizontal momentum if direction is changing
      if (direction != Direction.None && initialDirection() != direction) {
        debug(Tag.World, "$this canceling horizontal momentum")
        velocity.x = 0.0f
      }

      body.linearVelocity = velocity

      // apply the space jump impulse
      debug(Tag.World, "$this applying impulse")
      val center = body.worldCenter
      body.applyLinearImpulse(0.0f, -magnitude, center.x, center.y, true)
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Jumping2(context) else null
    }

    private fun initialDirection(): Direction {
      val velocity = body.linearVelocity

      return when {
        velocity.x < 0.0 -> Direction.Left
        velocity.x > 0.0 -> Direction.Right
        else -> Direction.None
      }
    }
  }

  class Jumping2(context: Context): FormState(context) {
    private val driftMagnitude = 5.0f

    override fun update(delta: Float) {
      super.update(delta)

      // apply directional influence
      val force = Vector2()
      if (controls.left.isPressed) {
        force.x -= driftMagnitude
      }

      if (controls.right.isPressed) {
        force.x += driftMagnitude
      }

      body.applyForceToCenter(force, true)
    }

    override fun nextState(): State? {
      return if (didLand()) Landing(context) else null
    }

    private fun didLand(): Boolean {
      assert(body.fixtureList.size != 0) { "body must have at least one fixture" }
      return contact.oriented(body.fixtureList.first(), Orientation.Bottom)
    }
  }

  class Landing(context: Context): FormState(context) {
    private val frameLength = 3

    override fun start() {
      super.start()
      controls.jump.requireUniquePress()
    }

    override fun nextState(): State? {
      return if (frame >= frameLength) Standing(context) else null
    }
  }
}
