package de.redlion.civilwar.collision;

import com.badlogic.gdx.math.collision.Sphere;

import de.redlion.civilwar.units.Soldier;

public class Octree {

	private final OctreeNode node;
	   
	   // define a quadtree extends as width and height, define quadtree depth.
	   public Octree(final float worldExtends, int worldDepth) {
	      node = new OctreeNode(0,0,0,worldExtends, worldDepth);
	   }

	   // insert a GameObject at the quadtree
	   public void insertObject(final Soldier obj) {
	      node.insertObject(obj, new Sphere(obj.instance.origin, obj.instance.getBoundingSphereRadius()));
	   }
	   
	   // clean the quadtree
	   public void clean() {
	      node.clean();
	   }
	
}
