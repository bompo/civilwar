package de.redlion.civilwar.collision;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import de.redlion.civilwar.Helper;

public class HeightMap {

	final int HEIGHTMAPSIZE = 256;

	public float[] map;
	public float[][] heightMap;

	BoundingBox boundingBox;

	public HeightMap(Model plane) {
		boundingBox = new BoundingBox();

		// get max bounds from model
		int len = plane.meshParts.size;
		for (int i = 0; i < len; i++) {
			MeshPart subMesh = plane.meshParts.get(i);
			BoundingBox bb = new BoundingBox();
			bb = subMesh.mesh.calculateBoundingBox();
			boundingBox.ext(bb);
		}

		heightMap = new float[HEIGHTMAPSIZE][HEIGHTMAPSIZE];

		// iterate through model and generate grid from vertices height
		len = plane.meshParts.size;
		for (int i = 0; i < len; i++) {
			MeshPart subMesh = plane.meshParts.get(i);
			
			map = new float[subMesh.mesh.getNumVertices() * 3];

			int j = 0;
			for (int n = 0; n < subMesh.mesh.getNumVertices() * 8; n = n + 8) {
				// y is height
				float x = subMesh.mesh.getVerticesBuffer().get(n);
				float y = subMesh.mesh.getVerticesBuffer().get(n + 1);
				float z = subMesh.mesh.getVerticesBuffer().get(n + 2);

				map[j] = y;
				map[j + 1] = z;
				map[j + 2] = x;

				j = j + 3;
			}
		}

		FileHandle fileIn = Gdx.files.external("heightMap.dat");
		if (!fileIn.exists()) {
			fileIn = Gdx.files.internal("data/heightMap.dat");
		}
		if (fileIn.exists()) {
			try {
				ObjectInputStream iis = new ObjectInputStream(fileIn.read());
				heightMap = (float[][]) iis.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			// ray collision test for each point of the height map
			Ray ray = new Ray(Vector3.X, Vector3.Y);
			Vector3 localIntersection = new Vector3();
			for (int x = 0; x < HEIGHTMAPSIZE; x++) {
				for (int z = 0; z < HEIGHTMAPSIZE; z++) {

					float x1 = Helper.map(x, 0, HEIGHTMAPSIZE, boundingBox.min.x, boundingBox.max.x);
					float z1 = Helper.map(z, 0, HEIGHTMAPSIZE, boundingBox.min.y, boundingBox.max.y);

					ray.set(new Vector3(x1, z1, -100), Vector3.Y);

					Intersector.intersectRayTriangles(ray, map, localIntersection);
					heightMap[x][z] = localIntersection.y;
				}
			}

			try {
				FileHandle fileOut = Gdx.files.external("heightMap.dat");
				fileOut.file().createNewFile();
				ObjectOutputStream oos = new ObjectOutputStream(fileOut.write(false));
				oos.writeObject(heightMap);
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}

	public float getHeight(float x, float z) {
		// TODO do fancy interpolation stuff or move this to GPU?
		int x1 = (int) Math.floor(Helper.map(x, boundingBox.min.x, boundingBox.max.x, 0, HEIGHTMAPSIZE));
		int z1 = (int) Math.floor(Helper.map(z, boundingBox.min.y, boundingBox.max.y, 0, HEIGHTMAPSIZE));

		return heightMap[x1][z1];
	}

}
