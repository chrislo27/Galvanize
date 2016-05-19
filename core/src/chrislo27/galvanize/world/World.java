package chrislo27.galvanize.world;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import chrislo27.galvanize.block.Block;
import chrislo27.galvanize.entity.Entity;
import chrislo27.galvanize.entity.living.EntityPlayer;
import ionium.aabbcollision.CollisionResolver;
import ionium.aabbcollision.PhysicsBody;
import ionium.benchmarking.TickBenchmark;
import ionium.registry.GlobalVariables;
import ionium.util.CoordPool;
import ionium.util.Coordinate;
import ionium.util.quadtree.QuadTree;

public class World {

	public Array<Entity> entities = new Array<>();
	private QuadTree<Entity> quadtree;
	private Array<Entity> entityRetrievalArray = new Array<>();
	public CollisionResolver collisionResolver;
	public Vector2 gravity = new Vector2(0, 25);
	public Pool<PhysicsBody> physicsBodyPool = new Pool<PhysicsBody>() {

		@Override
		protected PhysicsBody newObject() {
			return new PhysicsBody();
		}

	};

	public final int worldWidth, worldHeight;

	private Block[][] blocks;

	private EntityPlayer thePlayer;

	public World(int w, int h) {
		worldWidth = w;
		worldHeight = h;

		blocks = new Block[w][h];
		quadtree = new QuadTree(w, h);

		collisionResolver = new CollisionResolver(1f / GlobalVariables.ticks, 1f / Block.TILE_SIZE);
	}

	public void tickUpdate() {
		TickBenchmark.instance().start("collision");
		quadtree.clear();
		for (int i = 0; i < entities.size; i++) {
			Entity e = entities.get(i);

			quadtree.insert(e);
		}

		for (int i = entities.size - 1; i >= 0; i--) {
			Entity e = entities.get(i);

			e.movementUpdate();
		}
		TickBenchmark.instance().stop("collision");

		TickBenchmark.instance().start("entityUpdate");
		for (int i = entities.size - 1; i >= 0; i--) {
			Entity e = entities.get(i);

			e.tickUpdate();

			if (e.shouldBeRemoved()) {
				entities.removeIndex(i);
			}
		}
		TickBenchmark.instance().stop("entityUpdate");
	}

	public EntityPlayer getPlayer() {
		if (thePlayer == null) {

		}

		return thePlayer;
	}

	public Array<Entity> getNearbyCollidableEntities(Entity e) {
		entityRetrievalArray.clear();

		quadtree.retrieve(entityRetrievalArray, e);

		return entityRetrievalArray;
	}

	public QuadTree getQuadTree() {
		return quadtree;
	}

	public void getAllBlocksInArea(Array<Coordinate> array, float x, float y, float w, float h) {
		Coordinate topLeft = CoordPool.obtain().setPosition((int) (x), (int) (y + h));
		Coordinate topRight = CoordPool.obtain().setPosition((int) (x + w), (int) (y + h));
		Coordinate bottomLeft = CoordPool.obtain().setPosition((int) (x), (int) (y));

		for (int cx = topLeft.getX(); cx <= topRight.getX(); cx++) {
			for (int cy = bottomLeft.getY(); cy <= topLeft.getY(); cy++) {
				array.add(CoordPool.obtain().setPosition(cx, cy));
			}
		}

		CoordPool.free(topLeft);
		CoordPool.free(topRight);
		CoordPool.free(bottomLeft);
	}

	public void getAllBlocksInArea(Array<Coordinate> array, Rectangle rect) {
		getAllBlocksInArea(array, rect.x, rect.y, rect.width, rect.height);
	}

	public Block getBlock(int x, int y) {
		if (x < 0 || y < 0 || x >= worldWidth || y >= worldHeight) return null;

		return blocks[x][y];
	}

	public void setBlock(Block b, int x, int y) {
		if (x < 0 || y < 0 || x >= worldWidth || y >= worldHeight) return;

		blocks[x][y] = b;
	}

}
