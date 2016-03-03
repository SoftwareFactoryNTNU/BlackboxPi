package DigitalUnit;

import java.util.List;

import DigitalUnit.analyser.Analyser;
import DigitalUnit.car.CarData;
import DigitalUnit.data.DataBuffer;
import DigitalUnit.data.DataBufferListener;
import DigitalUnit.database.DBClient;
import DigitalUnit.server.GsonCollection;
import DigitalUnit.server.HttpServer;

public class WorkHandler implements DataBufferListener{
	boolean regularState = true;
	HttpServer server = new HttpServer();
	DataBuffer dataBuffer = new DataBuffer(this);
	
	/**Called upon when a complete carData object has been created. 
	 * Evaluates data to set system in correct state and sends data to database.
	 * If system recognizes a crash situation it will send data to server as well
	 * 
	 * @param data		CarData object representing a complete line of data from the car
	 */
	public void processData(CarData data) {
		if (regularState) {
			normalState(data);
		}
		else {
			crashState(data);
		}
	}
	
	private void normalState(CarData data) {
		DBClient.insert(data);
		if (Analyser.hasCrashed()) {
			List<CarData> dataSet = DBClient.getAll();
			server.sendLines(new GsonCollection(dataSet));
			regularState = false;
		}
	}
	
	private void crashState(CarData data) {
		DBClient.insert(data);
		server.sendLine(data);
		if (Analyser.hasCarStopped()) {
			regularState = true;
		}
	}
}