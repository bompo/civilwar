package de.redlion.rts.collision;

import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;

public class HeightMap {

	public HeightMap(StillModel plane) {
		//iterate through model and generate grid from vertices height
		int len = plane.subMeshes.length;
		for (int i = 0; i < len; i++) {
			StillSubMesh subMesh = plane.subMeshes[i];
			for(int n=0; n < subMesh.mesh.getNumVertices()*8; n=n+8) {
				//y is height
				float x = subMesh.mesh.getVerticesBuffer().get(n);
				float y = subMesh.mesh.getVerticesBuffer().get(n+1);
				float z = subMesh.mesh.getVerticesBuffer().get(n+2);
			}
			
		}
			
		
	}
	
}
