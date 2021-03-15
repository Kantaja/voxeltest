// MIT License
//
// Copyright(c) 2020 Jordan Peck (jordan.me2@gmail.com)
// Copyright(c) 2020 Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
// .'',;:cldxkO00KKXXNNWWWNNXKOkxdollcc::::::;:::ccllloooolllllllllooollc:,'...        ...........',;cldxkO000Okxdlc::;;;,,;;;::cclllllll
// ..',;:ldxO0KXXNNNNNNNNXXK0kxdolcc::::::;;;,,,,,,;;;;;;;;;;:::cclllllc:;'....       ...........',;:ldxO0KXXXK0Okxdolc::;;;;::cllodddddo
// ...',:loxO0KXNNNNNXXKK0Okxdolc::;::::::::;;;,,'''''.....''',;:clllllc:;,'............''''''''',;:loxO0KXNNNNNXK0Okxdollccccllodxxxxxxd
// ....';:ldkO0KXXXKK00Okxdolcc:;;;;;::cclllcc:;;,''..... ....',;clooddolcc:;;;;,,;;;;;::::;;;;;;:cloxk0KXNWWWWWWNXKK0Okxddoooddxxkkkkkxx
// .....';:ldxkOOOOOkxxdolcc:;;;,,,;;:cllooooolcc:;'...      ..,:codxkkkxddooollloooooooollcc:::::clodkO0KXNWWWWWWNNXK00Okxxxxxxxxkkkkxxx
// . ....';:cloddddo___________,,,,;;:clooddddoolc:,...      ..,:ldx__00OOOkkk___kkkkkkxxdollc::::cclodkO0KXXNNNNNNXXK0OOkxxxxxxxxxxxxddd
// .......',;:cccc:|           |,,,;;:cclooddddoll:;'..     ..';cox|  \KKK000|   |KK00OOkxdocc___;::clldxxkO0KKKKK00Okkxdddddddddddddddoo
// .......'',,,,,''|   ________|',,;;::cclloooooolc:;'......___:ldk|   \KK000|   |XKKK0Okxolc|   |;;::cclodxxkkkkxxdoolllcclllooodddooooo
// ''......''''....|   |  ....'',,,,;;;::cclloooollc:;,''.'|   |oxk|    \OOO0|   |KKK00Oxdoll|___|;;;;;::ccllllllcc::;;,,;;;:cclloooooooo
// ;;,''.......... |   |_____',,;;;____:___cllo________.___|   |___|     \xkk|   |KK_______ool___:::;________;;;_______...'',;;:ccclllloo
// c:;,''......... |         |:::/     '   |lo/        |           |      \dx|   |0/       \d|   |cc/        |'/       \......',,;;:ccllo
// ol:;,'..........|    _____|ll/    __    |o/   ______|____    ___|   |   \o|   |/   ___   \|   |o/   ______|/   ___   \ .......'',;:clo
// dlc;,...........|   |::clooo|    /  |   |x\___   \KXKKK0|   |dol|   |\   \|   |   |   |   |   |d\___   \..|   |  /   /       ....',:cl
// xoc;'...  .....'|   |llodddd|    \__|   |_____\   \KKK0O|   |lc:|   |'\       |   |___|   |   |_____\   \.|   |_/___/...      ...',;:c
// dlc;'... ....',;|   |oddddddo\          |          |Okkx|   |::;|   |..\      |\         /|   |          | \         |...    ....',;:c
// ol:,'.......',:c|___|xxxddollc\_____,___|_________/ddoll|___|,,,|___|...\_____|:\ ______/l|___|_________/...\________|'........',;::cc
// c:;'.......';:codxxkkkkxxolc::;::clodxkOO0OOkkxdollc::;;,,''''',,,,''''''''''',,'''''',;:loxkkOOkxol:;,'''',,;:ccllcc:;,'''''',;::ccll
// ;,'.......',:codxkOO0OOkxdlc:;,,;;:cldxxkkxxdolc:;;,,''.....'',;;:::;;,,,'''''........,;cldkO0KK0Okdoc::;;::cloodddoolc:;;;;;::ccllooo
// .........',;:lodxOO0000Okdoc:,,',,;:clloddoolc:;,''.......'',;:clooollc:;;,,''.......',:ldkOKXNNXX0Oxdolllloddxxxxxxdolccccccllooodddd
// .    .....';:cldxkO0000Okxol:;,''',,;::cccc:;,,'.......'',;:cldxxkkxxdolc:;;,'.......';coxOKXNWWWNXKOkxddddxxkkkkkkxdoollllooddxxxxkkk
//       ....',;:codxkO000OOxdoc:;,''',,,;;;;,''.......',,;:clodkO00000Okxolc::;,,''..',;:ldxOKXNWWWNNK0OkkkkkkkkkkkxxddooooodxxkOOOOO000
//       ....',;;clodxkkOOOkkdolc:;,,,,,,,,'..........,;:clodxkO0KKXKK0Okxdolcc::;;,,,;;:codkO0XXNNNNXKK0OOOOOkkkkxxdoollloodxkO0KKKXXXXX

package info.kuonteje.voxeltest.util.noise.impl;

public class NoiseCommon
{
	// @formatter:off
	
	private static final double[] GRADIENTS_2D = {
			0.130526192220052,  0.99144486137381,   0.38268343236509,   0.923879532511287,  0.608761429008721,  0.793353340291235,  0.793353340291235,  0.608761429008721,
			0.923879532511287,  0.38268343236509,   0.99144486137381,   0.130526192220051,  0.99144486137381,  -0.130526192220051,  0.923879532511287, -0.38268343236509,
			0.793353340291235, -0.60876142900872,   0.608761429008721, -0.793353340291235,  0.38268343236509,  -0.923879532511287,  0.130526192220052, -0.99144486137381,
			-0.130526192220052, -0.99144486137381,  -0.38268343236509,  -0.923879532511287, -0.608761429008721, -0.793353340291235, -0.793353340291235, -0.608761429008721,
			-0.923879532511287, -0.38268343236509,  -0.99144486137381,  -0.130526192220052, -0.99144486137381,   0.130526192220051, -0.923879532511287,  0.38268343236509,
			-0.793353340291235,  0.608761429008721, -0.608761429008721,  0.793353340291235, -0.38268343236509,   0.923879532511287, -0.130526192220052,  0.99144486137381,
			0.130526192220052,  0.99144486137381,   0.38268343236509,   0.923879532511287,  0.608761429008721,  0.793353340291235,  0.793353340291235,  0.608761429008721,
			0.923879532511287,  0.38268343236509,   0.99144486137381,   0.130526192220051,  0.99144486137381,  -0.130526192220051,  0.923879532511287, -0.38268343236509,
			0.793353340291235, -0.60876142900872,   0.608761429008721, -0.793353340291235,  0.38268343236509,  -0.923879532511287,  0.130526192220052, -0.99144486137381,
			-0.130526192220052, -0.99144486137381,  -0.38268343236509,  -0.923879532511287, -0.608761429008721, -0.793353340291235, -0.793353340291235, -0.608761429008721,
			-0.923879532511287, -0.38268343236509,  -0.99144486137381,  -0.130526192220052, -0.99144486137381,   0.130526192220051, -0.923879532511287,  0.38268343236509,
			-0.793353340291235,  0.608761429008721, -0.608761429008721,  0.793353340291235, -0.38268343236509,   0.923879532511287, -0.130526192220052,  0.99144486137381,
			0.130526192220052,  0.99144486137381,   0.38268343236509,   0.923879532511287,  0.608761429008721,  0.793353340291235,  0.793353340291235,  0.608761429008721,
			0.923879532511287,  0.38268343236509,   0.99144486137381,   0.130526192220051,  0.99144486137381,  -0.130526192220051,  0.923879532511287, -0.38268343236509,
			0.793353340291235, -0.60876142900872,   0.608761429008721, -0.793353340291235,  0.38268343236509,  -0.923879532511287,  0.130526192220052, -0.99144486137381,
			-0.130526192220052, -0.99144486137381,  -0.38268343236509,  -0.923879532511287, -0.608761429008721, -0.793353340291235, -0.793353340291235, -0.608761429008721,
			-0.923879532511287, -0.38268343236509,  -0.99144486137381,  -0.130526192220052, -0.99144486137381,   0.130526192220051, -0.923879532511287,  0.38268343236509,
			-0.793353340291235,  0.608761429008721, -0.608761429008721,  0.793353340291235, -0.38268343236509,   0.923879532511287, -0.130526192220052,  0.99144486137381,
			0.130526192220052,  0.99144486137381,   0.38268343236509,   0.923879532511287,  0.608761429008721,  0.793353340291235,  0.793353340291235,  0.608761429008721,
			0.923879532511287,  0.38268343236509,   0.99144486137381,   0.130526192220051,  0.99144486137381,  -0.130526192220051,  0.923879532511287, -0.38268343236509,
			0.793353340291235, -0.60876142900872,   0.608761429008721, -0.793353340291235,  0.38268343236509,  -0.923879532511287,  0.130526192220052, -0.99144486137381,
			-0.130526192220052, -0.99144486137381,  -0.38268343236509,  -0.923879532511287, -0.608761429008721, -0.793353340291235, -0.793353340291235, -0.608761429008721,
			-0.923879532511287, -0.38268343236509,  -0.99144486137381,  -0.130526192220052, -0.99144486137381,   0.130526192220051, -0.923879532511287,  0.38268343236509,
			-0.793353340291235,  0.608761429008721, -0.608761429008721,  0.793353340291235, -0.38268343236509,   0.923879532511287, -0.130526192220052,  0.99144486137381,
			0.130526192220052,  0.99144486137381,   0.38268343236509,   0.923879532511287,  0.608761429008721,  0.793353340291235,  0.793353340291235,  0.608761429008721,
			0.923879532511287,  0.38268343236509,   0.99144486137381,   0.130526192220051,  0.99144486137381,  -0.130526192220051,  0.923879532511287, -0.38268343236509,
			0.793353340291235, -0.60876142900872,   0.608761429008721, -0.793353340291235,  0.38268343236509,  -0.923879532511287,  0.130526192220052, -0.99144486137381,
			-0.130526192220052, -0.99144486137381,  -0.38268343236509,  -0.923879532511287, -0.608761429008721, -0.793353340291235, -0.793353340291235, -0.608761429008721,
			-0.923879532511287, -0.38268343236509,  -0.99144486137381,  -0.130526192220052, -0.99144486137381,   0.130526192220051, -0.923879532511287,  0.38268343236509,
			-0.793353340291235,  0.608761429008721, -0.608761429008721,  0.793353340291235, -0.38268343236509,   0.923879532511287, -0.130526192220052,  0.99144486137381,
			0.38268343236509,   0.923879532511287,  0.923879532511287,  0.38268343236509,   0.923879532511287, -0.38268343236509,   0.38268343236509,  -0.923879532511287,
			-0.38268343236509,  -0.923879532511287, -0.923879532511287, -0.38268343236509,  -0.923879532511287,  0.38268343236509,  -0.38268343236509,   0.923879532511287
	};
	
	private static final double[] GRADIENTS_3D = {
			0.0, 1.0, 1.0, 0.0,  0.0,-1.0, 1.0, 0.0,  0.0, 1.0,-1.0, 0.0,  0.0,-1.0,-1.0, 0.0,
			1.0, 0.0, 1.0, 0.0, -1.0, 0.0, 1.0, 0.0,  1.0, 0.0,-1.0, 0.0, -1.0, 0.0,-1.0, 0.0,
			1.0, 1.0, 0.0, 0.0, -1.0, 1.0, 0.0, 0.0,  1.0,-1.0, 0.0, 0.0, -1.0,-1.0, 0.0, 0.0,
			0.0, 1.0, 1.0, 0.0,  0.0,-1.0, 1.0, 0.0,  0.0, 1.0,-1.0, 0.0,  0.0,-1.0,-1.0, 0.0,
			1.0, 0.0, 1.0, 0.0, -1.0, 0.0, 1.0, 0.0,  1.0, 0.0,-1.0, 0.0, -1.0, 0.0,-1.0, 0.0,
			1.0, 1.0, 0.0, 0.0, -1.0, 1.0, 0.0, 0.0,  1.0,-1.0, 0.0, 0.0, -1.0,-1.0, 0.0, 0.0,
			0.0, 1.0, 1.0, 0.0,  0.0,-1.0, 1.0, 0.0,  0.0, 1.0,-1.0, 0.0,  0.0,-1.0,-1.0, 0.0,
			1.0, 0.0, 1.0, 0.0, -1.0, 0.0, 1.0, 0.0,  1.0, 0.0,-1.0, 0.0, -1.0, 0.0,-1.0, 0.0,
			1.0, 1.0, 0.0, 0.0, -1.0, 1.0, 0.0, 0.0,  1.0,-1.0, 0.0, 0.0, -1.0,-1.0, 0.0, 0.0,
			0.0, 1.0, 1.0, 0.0,  0.0,-1.0, 1.0, 0.0,  0.0, 1.0,-1.0, 0.0,  0.0,-1.0,-1.0, 0.0,
			1.0, 0.0, 1.0, 0.0, -1.0, 0.0, 1.0, 0.0,  1.0, 0.0,-1.0, 0.0, -1.0, 0.0,-1.0, 0.0,
			1.0, 1.0, 0.0, 0.0, -1.0, 1.0, 0.0, 0.0,  1.0,-1.0, 0.0, 0.0, -1.0,-1.0, 0.0, 0.0,
			0.0, 1.0, 1.0, 0.0,  0.0,-1.0, 1.0, 0.0,  0.0, 1.0,-1.0, 0.0,  0.0,-1.0,-1.0, 0.0,
			1.0, 0.0, 1.0, 0.0, -1.0, 0.0, 1.0, 0.0,  1.0, 0.0,-1.0, 0.0, -1.0, 0.0,-1.0, 0.0,
			1.0, 1.0, 0.0, 0.0, -1.0, 1.0, 0.0, 0.0,  1.0,-1.0, 0.0, 0.0, -1.0,-1.0, 0.0, 0.0,
			1.0, 1.0, 0.0, 0.0,  0.0,-1.0, 1.0, 0.0, -1.0, 1.0, 0.0, 0.0,  0.0,-1.0,-1.0, 0.0
	};
	
	// @formatter:on
	
	public static final double SQRT3 = Math.sqrt(3.0);
	
	public static final int PRIME_X = 501125321;
	public static final int PRIME_Y = 1136930381;
	public static final int PRIME_Z = 1720413743;
	
	public static int hash(int seed, int xPrimed, int yPrimed)
	{
		int hash = seed ^ xPrimed ^ yPrimed;
		
		hash *= 0x27D4EB2D;
		
		return hash;
	}
	
	public static int hash(int seed, int xPrimed, int yPrimed, int zPrimed)
	{
		int hash = seed ^ xPrimed ^ yPrimed ^ zPrimed;
		
		hash *= 0x27D4EB2D;
		
		return hash;
	}
	
	public static double valCoord(int seed, int xPrimed, int yPrimed)
	{
		int hash = hash(seed, xPrimed, yPrimed);
		
		hash *= hash;
		hash ^= hash << 19;
		
		return hash * (1.0 / 2147483648.0);
	}
	
	public static double valCoord(int seed, int xPrimed, int yPrimed, int zPrimed)
	{
		int hash = hash(seed, xPrimed, yPrimed, zPrimed);
		
		hash *= hash;
		hash ^= hash << 19;
		
		return hash * (1.0 / 2147483648.0);
	}
	
	public static double gradCoord(int seed, int xPrimed, int yPrimed, double xd, double yd)
	{
		int hash = hash(seed, xPrimed, yPrimed);
		
		hash ^= hash >> 15;
		hash &= 127 << 1;
		
		double xg = GRADIENTS_2D[hash];
		double yg = GRADIENTS_2D[hash | 1];
		
		return xd * xg + yd * yg;
	}
	
	public static double gradCoord(int seed, int xPrimed, int yPrimed, int zPrimed, double xd, double yd, double zd)
	{
		int hash = hash(seed, xPrimed, yPrimed, zPrimed);
		
		hash ^= hash >> 15;
		hash &= 63 << 2;
		
		double xg = GRADIENTS_3D[hash];
		double yg = GRADIENTS_3D[hash | 1];
		double zg = GRADIENTS_3D[hash | 2];
		
		return xd * xg + yd * yg + zd * zg;
	}
	
	public static int fastRound(double f)
	{
		return f >= 0.0 ? (int)(f + 0.5) : (int)(f - 0.5);
	}
	
	public static double fractalBounding(int octaves, double gain)
	{
		gain = Math.abs(gain);
		
		double amp = gain;
		double ampFractal = 1.0;
		
		for(int i = 1; i < octaves; i++)
		{
			ampFractal += amp;
			amp *= gain;
		}
		
		return 1.0 / ampFractal;
	}
}
