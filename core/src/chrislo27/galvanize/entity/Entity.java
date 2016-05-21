package chrislo27.galvanize.entity;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.render.entity.EntityRenderer;
import chrislo27.galvanize.world.World;
import ionium.aabbcollision.CollisionResult;
import ionium.aabbcollision.PhysicsBody;
import ionium.registry.GlobalVariables;
import ionium.util.CoordPool;
import ionium.util.Coordinate;
import ionium.util.MathHelper;
import ionium.util.quadtree.QuadRectangleable;

public abstract class Entity implements QuadRectangleable {

	protected static final Array<Coordinate> tempCoordArray = new Array<>();
	protected static final Array<PhysicsBody> tempBodyArray = new Array<>();

	public World world;
	public EntityRenderer renderer;
	public PhysicsBody physicsBody = new PhysicsBody();
	public transient Vector2 lastKnownPosition = new Vector2();

	public Vector2 maxSpeed = new Vector2(10, 10);
	public Vector2 accSpeed = new Vector2(maxSpeed.x * 2, maxSpeed.y * 2);
	public float jumpHeight = MathHelper.getJumpVelo(9.8f, 1);
	public float dragCoefficient = 4;
	public float frictionCoefficient = 1;

	public Vector2 collidingNormal = new Vector2();

	public Entity(World world, float x, float y, float width, float height) {
		this.world = world;
		physicsBody.setBounds(x, y, width, height);
		lastKnownPosition.set(x, y);
	}

	public void movementUpdate() {
		lastKnownPosition.set(physicsBody.bounds.x, physicsBody.bounds.y);
		physicsBody.velocity.add(world.gravity.x / GlobalVariables.ticks,
				world.gravity.y / GlobalVariables.ticks);

		CollisionResult collisionResult;
		Array<PhysicsBody> bodies = world.collisionResolver.getTempBodyArray();

		// prep temp arrays
		tempCoordArray.clear();
		tempBodyArray.clear();

		// set tmp1 to the original bounds
		Rectangle.tmp.set(physicsBody.bounds);

		// expand the bounds so we have blocks around us
		physicsBody.bounds.x -= 1;
		physicsBody.bounds.y -= 1;
		physicsBody.bounds.width += 2;
		physicsBody.bounds.height += 2;

		// add the coordinates from CoordPool
		world.getAllBlocksInArea(tempCoordArray,
				physicsBody.getAreaOfTravel(world.collisionResolver.timeScale));

		// set tmp2 to the expanded bounds
		Rectangle.tmp2.set(physicsBody.bounds);
		// reset bounds
		physicsBody.bounds.set(Rectangle.tmp);

		// add all the physicsbodies to the array
		for (int i = 0; i < tempCoordArray.size; i++) {
			Coordinate c = tempCoordArray.get(i);
			Block b = world.getBlock(c.getX(), c.getY());

			if (b == null) {
				continue;
			}

			PhysicsBody body = world.physicsBodyPool.obtain();
			PhysicsBody result = b.getPhysicsBody(body, c.getX(), c.getY());

			if (result == null) {
				world.physicsBodyPool.free(body);

				continue;
			}

			tempBodyArray.add(result);
			bodies.add(result);
		}

		// free coordinates, we're done
		CoordPool.freeAll(tempCoordArray);
		tempCoordArray.clear();

		// add entity physics
		Array<Entity> nearby = world.getNearbyCollidableEntities(this);
		Rectangle pathBounds = this.physicsBody.getAreaOfTravel(world.collisionResolver.timeScale);
		for (int i = 0; i < nearby.size; i++) {
			Entity e = nearby.get(i);

			if (e == this) continue;
			if (!e.physicsBody.bounds.overlaps(pathBounds)) continue;
			if (!canThisCollideInto(e) || !e.canOtherCollideWith(this)) continue;

			bodies.add(e.physicsBody);
		}

		// resolve collision
		collisionResult = world.collisionResolver.resolveCollisionBetweenBodies(this.physicsBody,
				bodies, Rectangle.tmp2);

		// set position
		physicsBody.bounds.x = collisionResult.newPosition.x;
		physicsBody.bounds.y = collisionResult.newPosition.y;

		collidingNormal.set(collisionResult.normal);

		if (collisionResult.normal.y == 1) {
			float avgFriction = getAverageFriction(nearby);
			int oldSign = (int) Math.signum(physicsBody.velocity.x);

			physicsBody.velocity.x += ((avgFriction * Math.abs(world.gravity.y) * dragCoefficient)
					/ GlobalVariables.ticks) * -Math.signum(physicsBody.velocity.x);

			if (((int) Math.signum(physicsBody.velocity.x)) != oldSign) {
				physicsBody.velocity.x = 0;
			}
		}

		// free physicsbodies, result
		world.collisionResolver.freeResult(collisionResult);
		world.physicsBodyPool.freeAll(tempBodyArray);
		tempBodyArray.clear();
	}

	/**
	 * If this entity can collide into the other one. Do not confuse with
	 * canOtherCollideWith.
	 * @param e
	 * @return
	 */
	public boolean canThisCollideInto(Entity e) {
		return true;
	}

	/**
	 * If the other entity can collide into this one. Do not confuse with
	 * canThisCollideInto.
	 * @param e
	 * @return
	 */
	public boolean canOtherCollideWith(Entity e) {
		return true;
	}

	public float getAverageFriction(Array<Entity> nearby) {
		return (getAverageEntityFriction(nearby) + getAverageGroundFriction()) * 0.5f;
	}

	public float getAverageEntityFriction(Array<Entity> nearby) {
		float friction = 0;
		int counted = 0;

		for (int i = 0; i < nearby.size; i++) {
			Entity e = nearby.get(i);

			if (e == this) continue;

			if (MathUtils.isEqual(e.physicsBody.bounds.y + e.physicsBody.bounds.height,
					this.physicsBody.bounds.y, world.collisionResolver.tolerance)) {
				friction += e.frictionCoefficient;
				counted++;
			}
		}

		return counted == 0 ? 0 : friction / counted;
	}

	public float getAverageGroundFriction() {
		int rangeStart = (int) (physicsBody.bounds.x);
		int rangeEnd = (int) (physicsBody.bounds.x + physicsBody.bounds.width);
		float total = 0;
		int blocks = 0;

		for (int x = rangeStart; x <= rangeEnd; x++) {
			Block b = world.getBlock(x, (int) (physicsBody.bounds.y - 1));

			if (b != null) {
				blocks++;
				total += b.friction;
			}
		}

		return (blocks == 0 ? 0 : total / blocks);
	}

	public void tickUpdate() {

	}

	public boolean shouldBeRemoved() {
		return false;
	}

	public void move(float amtX, float amtY) {
		amtX += getAverageFriction(world.getNearbyCollidableEntities(this)) * amtX;

		if ((Math.abs(physicsBody.velocity.x) <= maxSpeed.x
				|| Math.signum(amtX) != Math.signum(physicsBody.velocity.x)) && amtX != 0) {
			if (amtX > 0) {
				physicsBody.velocity.x = Math.min(maxSpeed.x,
						physicsBody.velocity.x + amtX * accSpeed.x);
			} else {
				physicsBody.velocity.x = Math.max(-maxSpeed.x,
						physicsBody.velocity.x + amtX * accSpeed.x);
			}
		}

		if ((Math.abs(physicsBody.velocity.y) <= maxSpeed.y
				|| Math.signum(amtY) != Math.signum(physicsBody.velocity.y)) && amtY != 0) {
			if (amtY > 0) {
				physicsBody.velocity.y = Math.min(maxSpeed.y,
						physicsBody.velocity.y + amtY * accSpeed.y);
			} else {
				physicsBody.velocity.y = Math.max(-maxSpeed.y,
						physicsBody.velocity.y + amtY * accSpeed.y);
			}
		}
	}

	public boolean isColliding() {
		return collidingNormal.x != 0 || collidingNormal.y != 0;
	}

	public void jump() {
		if (collidingNormal.y != 1) return;

		physicsBody.velocity.y += jumpHeight;
	}

	@Override
	public float getX() {
		return physicsBody.bounds.x;
	}

	@Override
	public float getY() {
		return physicsBody.bounds.y;
	}

	@Override
	public float getWidth() {
		return physicsBody.bounds.width;
	}

	@Override
	public float getHeight() {
		return physicsBody.bounds.height;
	}

}