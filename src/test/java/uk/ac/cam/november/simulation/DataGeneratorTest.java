package uk.ac.cam.november.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.gson.JsonObject;

public class DataGeneratorTest {
	
	/* createDefaultPacket */
	
	@Test
	public void defaultPacketShouldContainAllFields(){
		JsonObject packet = DataGenerator.createDefaultPacket();
		assertTrue(packet.has("timestamp"));
		assertTrue(packet.has("prio"));
		assertTrue(packet.has("src"));
		assertTrue(packet.has("dst"));
		assertTrue(packet.has("fields"));
	}
	
	@Test
	public void defaultPacketShouldHaveCorrectDefaultValues(){
		JsonObject packet = DataGenerator.createDefaultPacket();
		assertEquals(DataGenerator.DEFAULT_PRIORITY, packet.get("prio").getAsInt());
		assertEquals(DataGenerator.DEFAULT_SRC, packet.get("src").getAsInt());
		assertEquals(DataGenerator.DEFAULT_DEST, packet.get("dst").getAsInt());
		assertEquals(23, packet.get("timestamp").getAsString().length());
	}
	
	/* generateVesselHeadingPacket */
	
	@Test
	public void vesselHeadingPacketsShouldCheckInputRange() {
		try{ DataGenerator.generateVesselHeadingPacket(-1.0, 0, 0); fail("Heading did not catch <0"); } catch(IllegalArgumentException e) {}
		try{ DataGenerator.generateVesselHeadingPacket(370, 0, 0); fail("Heading did not catch >360"); } catch(IllegalArgumentException e) {}
		try{ DataGenerator.generateVesselHeadingPacket(0, -200, 0); fail("Deviation did not catch <-180"); } catch(IllegalArgumentException e) {}
		try{ DataGenerator.generateVesselHeadingPacket(0, 200, 0); fail("Deviation did not catch >180"); } catch(IllegalArgumentException e) {}
		try{ DataGenerator.generateVesselHeadingPacket(0, 0, -200); fail("Variation did not catch <-180"); } catch(IllegalArgumentException e) {}
		try{ DataGenerator.generateVesselHeadingPacket(0, 0, 200); fail("Variation did not catch >180"); } catch(IllegalArgumentException e) {}
	}
	
	@Test
	public void vesselHeadingPacketShouldContainCorrectFields() {
		JsonObject packet = DataGenerator.generateVesselHeadingPacket(100.0, 1.0, -1.0);
		JsonObject fields = packet.getAsJsonObject("fields");
		
		assertEquals(packet.get("pgn").getAsInt(), 127250);
		
		assertEquals(fields.get("Heading").getAsDouble(), 100.0, 0.01);
		assertEquals(fields.get("Deviation").getAsDouble(), 1.0, 0.01);
		assertEquals(fields.get("Variation").getAsDouble(), -1.0, 0.01);
	}
	
	/* generateWaterDepthPacket */
	
	@Test
	public void waterDepthPacketShouldCheckInputRange() {
		try{ DataGenerator.generateWaterDepthPacket(-1.0, 0); fail("Water Depth did not catch <0"); } catch(IllegalArgumentException e) {}
	}
	
	@Test
	public void waterDepthPacketShouldContainCorrectFields() {
		JsonObject packet = DataGenerator.generateWaterDepthPacket(100.0, 1.0);
		JsonObject fields = packet.getAsJsonObject("fields");
		
		assertEquals(packet.get("pgn").getAsInt(), 128267);
		
		assertEquals(fields.get("Depth").getAsDouble(), 100.0, 0.01);
		assertEquals(fields.get("Offset").getAsDouble(), 1.0, 0.01);
	}
	
}
