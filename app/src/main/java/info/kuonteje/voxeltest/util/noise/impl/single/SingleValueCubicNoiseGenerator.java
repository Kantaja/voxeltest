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

package info.kuonteje.voxeltest.util.noise.impl.single;

import info.kuonteje.voxeltest.util.MathUtil;
import info.kuonteje.voxeltest.util.noise.impl.INoiseSource;
import info.kuonteje.voxeltest.util.noise.impl.NoiseCommon;

public class SingleValueCubicNoiseGenerator implements INoiseSource
{
	@Override
	public double _noiseImpl(int seed, double x, double y)
	{
		int x1 = MathUtil.fastFloor(x);
		int y1 = MathUtil.fastFloor(y);
		
		double xs = x - x1;
		double ys = y - y1;
		
		x1 *= NoiseCommon.PRIME_X;
		y1 *= NoiseCommon.PRIME_Y;
		
		int x0 = x1 - NoiseCommon.PRIME_X;
		int y0 = y1 - NoiseCommon.PRIME_Y;
		
		int x2 = x1 + NoiseCommon.PRIME_X;
		int y2 = y1 + NoiseCommon.PRIME_Y;
		
		int x3 = x1 + (NoiseCommon.PRIME_X << 1);
		int y3 = y1 + (NoiseCommon.PRIME_Y << 1);
		
		return cubicLerp(
				cubicLerp(
						NoiseCommon.valCoord(seed, x0, y0),
						NoiseCommon.valCoord(seed, x1, y0),
						NoiseCommon.valCoord(seed, x2, y0),
						NoiseCommon.valCoord(seed, x3, y0),
						xs
						),
				cubicLerp(
						NoiseCommon.valCoord(seed, x0, y1),
						NoiseCommon.valCoord(seed, x1, y1),
						NoiseCommon.valCoord(seed, x2, y1),
						NoiseCommon.valCoord(seed, x3, y1),
						xs
						),
				cubicLerp(
						NoiseCommon.valCoord(seed, x0, y2),
						NoiseCommon.valCoord(seed, x1, y2),
						NoiseCommon.valCoord(seed, x2, y2),
						NoiseCommon.valCoord(seed, x3, y2),
						xs
						),
				cubicLerp(
						NoiseCommon.valCoord(seed, x0, y3),
						NoiseCommon.valCoord(seed, x1, y3),
						NoiseCommon.valCoord(seed, x2, y3),
						NoiseCommon.valCoord(seed, x3, y3),
						xs
						),
				ys
				) * (1 / (1.5 * 1.5));
	}
	
	@Override
	public double _noiseImpl(int seed, double x, double y, double z)
	{
		int x1 = MathUtil.fastFloor(x);
		int y1 = MathUtil.fastFloor(y);
		int z1 = MathUtil.fastFloor(z);
		
		double xs = x - x1;
		double ys = y - y1;
		double zs = z - z1;
		
		x1 *= NoiseCommon.PRIME_X;
		y1 *= NoiseCommon.PRIME_Y;
		z1 *= NoiseCommon.PRIME_Z;
		
		int x0 = x1 - NoiseCommon.PRIME_X;
		int y0 = y1 - NoiseCommon.PRIME_Y;
		int z0 = z1 - NoiseCommon.PRIME_Z;
		
		int x2 = x1 + NoiseCommon.PRIME_X;
		int y2 = y1 + NoiseCommon.PRIME_Y;
		int z2 = z1 + NoiseCommon.PRIME_Z;
		
		int x3 = x1 + (NoiseCommon.PRIME_X << 1);
		int y3 = y1 + (NoiseCommon.PRIME_Y << 1);
		int z3 = z1 + (NoiseCommon.PRIME_Z << 1);
		
		return cubicLerp(
				cubicLerp(
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y0, z0),
								NoiseCommon.valCoord(seed, x1, y0, z0),
								NoiseCommon.valCoord(seed, x2, y0, z0),
								NoiseCommon.valCoord(seed, x3, y0, z0),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y1, z0),
								NoiseCommon.valCoord(seed, x1, y1, z0),
								NoiseCommon.valCoord(seed, x2, y1, z0),
								NoiseCommon.valCoord(seed, x3, y1, z0),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y2, z0),
								NoiseCommon.valCoord(seed, x1, y2, z0),
								NoiseCommon.valCoord(seed, x2, y2, z0),
								NoiseCommon.valCoord(seed, x3, y2, z0),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y3, z0),
								NoiseCommon.valCoord(seed, x1, y3, z0),
								NoiseCommon.valCoord(seed, x2, y3, z0),
								NoiseCommon.valCoord(seed, x3, y3, z0),
								xs
								),
						ys
						),
				cubicLerp(
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y0, z1),
								NoiseCommon.valCoord(seed, x1, y0, z1),
								NoiseCommon.valCoord(seed, x2, y0, z1),
								NoiseCommon.valCoord(seed, x3, y0, z1),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y1, z1),
								NoiseCommon.valCoord(seed, x1, y1, z1),
								NoiseCommon.valCoord(seed, x2, y1, z1),
								NoiseCommon.valCoord(seed, x3, y1, z1),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y2, z1),
								NoiseCommon.valCoord(seed, x1, y2, z1),
								NoiseCommon.valCoord(seed, x2, y2, z1),
								NoiseCommon.valCoord(seed, x3, y2, z1),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y3, z1),
								NoiseCommon.valCoord(seed, x1, y3, z1),
								NoiseCommon.valCoord(seed, x2, y3, z1),
								NoiseCommon.valCoord(seed, x3, y3, z1),
								xs
								),
						ys
						),
				cubicLerp(
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y0, z2),
								NoiseCommon.valCoord(seed, x1, y0, z2),
								NoiseCommon.valCoord(seed, x2, y0, z2),
								NoiseCommon.valCoord(seed, x3, y0, z2),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y1, z2),
								NoiseCommon.valCoord(seed, x1, y1, z2),
								NoiseCommon.valCoord(seed, x2, y1, z2),
								NoiseCommon.valCoord(seed, x3, y1, z2),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y2, z2),
								NoiseCommon.valCoord(seed, x1, y2, z2),
								NoiseCommon.valCoord(seed, x2, y2, z2),
								NoiseCommon.valCoord(seed, x3, y2, z2),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y3, z2),
								NoiseCommon.valCoord(seed, x1, y3, z2),
								NoiseCommon.valCoord(seed, x2, y3, z2),
								NoiseCommon.valCoord(seed, x3, y3, z2),
								xs
								),
						ys
						),
				cubicLerp(
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y0, z3),
								NoiseCommon.valCoord(seed, x1, y0, z3),
								NoiseCommon.valCoord(seed, x2, y0, z3),
								NoiseCommon.valCoord(seed, x3, y0, z3),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y1, z3),
								NoiseCommon.valCoord(seed, x1, y1, z3),
								NoiseCommon.valCoord(seed, x2, y1, z3),
								NoiseCommon.valCoord(seed, x3, y1, z3),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y2, z3),
								NoiseCommon.valCoord(seed, x1, y2, z3),
								NoiseCommon.valCoord(seed, x2, y2, z3),
								NoiseCommon.valCoord(seed, x3, y2, z3),
								xs
								),
						cubicLerp(
								NoiseCommon.valCoord(seed, x0, y3, z3),
								NoiseCommon.valCoord(seed, x1, y3, z3),
								NoiseCommon.valCoord(seed, x2, y3, z3),
								NoiseCommon.valCoord(seed, x3, y3, z3),
								xs
								),
						ys
						),
				zs
				) * (1 / (1.5 * 1.5 * 1.5));
	}
	
	private static double cubicLerp(double a, double b, double c, double d, double t)
	{
		double p = (d - c) - (a - b);
		return t * t * t * p + t * t * ((a - b) - p) + t * (c - a) + b;
	}
}
