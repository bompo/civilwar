package de.redlion.civilwar.collision;

import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.redlion.civilwar.Helper;

public class HeightMap {
	
	public float[] map;
	public float[][] heightMap; 
	
	BoundingBox boundingBox;

	public HeightMap(StillModel plane) {
		boundingBox = new BoundingBox();
		
		//get max bounds from model
		int len = plane.subMeshes.length;
		for (int i = 0; i < len; i++) {
			StillSubMesh subMesh = plane.subMeshes[i];
			BoundingBox bb = new BoundingBox();
			subMesh.getBoundingBox(bb);
			boundingBox.ext(bb);
		}
		
		System.out.println(boundingBox);
		
		heightMap = new float[513][513];
				
		//iterate through model and generate grid from vertices height
		len = plane.subMeshes.length;
		for (int i = 0; i < len; i++) {
			StillSubMesh subMesh = plane.subMeshes[i];
			
			map = new float[subMesh.mesh.getNumVertices()*3];
			
			int j = 0;
			for(int n=0; n < subMesh.mesh.getNumVertices()*8; n=n+8) {
				//y is height
				float x = subMesh.mesh.getVerticesBuffer().get(n);
				float y = subMesh.mesh.getVerticesBuffer().get(n+1);
				float z = subMesh.mesh.getVerticesBuffer().get(n+2);
				
				map[j] = x;
				map[j + 1] = y;
				map[j + 2] = z;
				
				j = j + 3;
				
				//scale value to 512x512 height map
				int x1 = (int) Math.floor(Helper.map(x, boundingBox.min.x, boundingBox.max.x, 0, 512));
				int z1 = (int) Math.floor(Helper.map(z, boundingBox.min.z, boundingBox.max.z, 0, 512));
				
				heightMap[x1][z1] = y;
			}
			
		}
			
		
	}
	
}
