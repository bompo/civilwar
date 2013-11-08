package de.redlion.civilwar.collision;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Sphere;

import de.redlion.civilwar.units.Soldier;

public class OctreeNode {
	   private final int currDepth; // the current depth of this node
	   private final Vector3 center; // the center of this node
	   private final OctreeNode[] nodes; // the child nodes
	   
	   private final ArrayList<Soldier> objects; // the objects stored at this node
	   
	   public OctreeNode(float centerX, float centerY, float centerZ, float halfWidth, int stopDepth) {
	      this.currDepth = stopDepth;
	      
	      // set Vector to current x-y-z values
	      this.center = new Vector3(centerX, centerY, centerZ);
	      
	      this.objects = new ArrayList<Soldier>();
	      
	      float offsetX = 0.0f;
	      float offsetY = 0.0f;
	      float offsetZ = 0.0f;
	      
	      if (stopDepth > 0) {
	         // create 4 child nodes as long as depth is still greater than 0
	         this.nodes = new OctreeNode[8];
	         
	         // halve child nodes size
	         float step = halfWidth * 0.5f;
	         
	         // loop through and create new child nodes
	         for (int i = 0; i < 8; i++) {
	            
	            // compute the offsets of the child nodes
	            offsetX = (((i & 1) == 0) ? step : -step);
	            offsetY = (((i & 2) == 0) ? step : -step);
	            offsetZ = (((i & 4) == 0) ? step : -step);
	            
	            nodes[i] = new OctreeNode(centerX + offsetX, centerY + offsetY, centerZ + offsetZ, step, stopDepth - 1);
	         }   
	      }
	      else {
	         this.nodes = null;
	      }
	   }
	   
	   public void insertObject(final Soldier obj, final Sphere collider) {
	      int index = 0; // get child node index as 0 initially
	      boolean straddle = false; // set straddling to false
	      float delta;
	      
	      // get the raw arrays, makes it easier to run these in a loop
	      final float[] objPos = {collider.center.x,collider.center.y,collider.center.z};
	      final float[] nodePos = {center.x,center.y,center.z};
	      
	      for (int i = 0; i < 3; i++) {
	         // compute the delta, nodePos Vector index - objPos Vector
	         delta = nodePos[i] - objPos[i];
	         
	         // if the absolute of delta is less than or equal to radius object straddling, break
	         if (Math.abs(delta) <= collider.radius) {
	            straddle = true;
	            break;
	         }
	         
	         // compute the index to isnert to child node
	         if (delta > 0.0f) {
	            index |= (1 << i);
	         }
	      }
	      
	      if (!straddle && currDepth > 0) {
	         // not straddling, insert to child at index
	         nodes[index].insertObject(obj, collider);
	      }
	      else {
	         // straddling, insert to this node
	         objects.add(obj);
	      }
	   }
	   
	   public void clean() {
	      objects.clear();
	      
	      // clean children if available
	      if (currDepth > 0) {
	         for (int i = 0; i < 8; i++) {
	            nodes[i].clean();
	         }
	      }
	   }
	}
