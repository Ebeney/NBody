

class Buffer{
	// Classe qui gère le tampon entre le calcul de point et l'affichage
	// ELle est aussi utilisé comme mémoire des positions et vitesses des points
	
	Double[][][] buff;
	
	// Constructeur
	public Buffer() {
		// buffer_size correspond au nombre de frames calculées gardées en mémoire, et 4 aux couples positions x,y et vitesse x,y
		buff = new Double[Simulator.buffer_size][Simulator.N][4];
	}
	
	// Ajout d'un point au buffer
	public void write(long cur_frame, int pointID, String attribute, double value) {
		cur_frame = cur_frame % Simulator.buffer_size;
		int buffpos = (int) cur_frame;
		switch (attribute) {
			case "x": 	buff[buffpos][pointID][0] = value;
						break;
			case "y": 	buff[buffpos][pointID][1] = value;
						break;
			case "vx":	buff[buffpos][pointID][2] = value;
						break;
			case "vy":	buff[buffpos][pointID][3] = value;
						break;
		}
	}
	
	
	// Lecture d'une frame par l'affichage
	public double read(long cur_frame, int pointID, String attribute) {
		cur_frame = cur_frame % Simulator.buffer_size;
		int buffpos = (int) cur_frame;
		switch (attribute) {
			case "x": 	return buff[buffpos][pointID][0];
			case "y": 	return buff[buffpos][pointID][1];
			case "vx":	return buff[buffpos][pointID][2];
		}
		return buff[buffpos][pointID][3]; //case "vy"
	}
	
}