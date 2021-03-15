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

public class SinglePerlinNoiseGenerator implements INoiseSource
{
	@Override
	public double _noiseImpl(int seed, double x, double y)
	{
		int x0 = MathUtil.fastFloor(x);
		int y0 = MathUtil.fastFloor(y);
		
		double xd0 = x - x0;
		double yd0 = y - y0;
		
		double xd1 = xd0 - 1;
		double yd1 = yd0 - 1;
		
		double xs = interpQuintic(xd0);
		double ys = interpQuintic(yd0);
		
		x0 *= NoiseCommon.PRIME_X;
		y0 *= NoiseCommon.PRIME_Y;
		
		int x1 = x0 + NoiseCommon.PRIME_X;
		int y1 = y0 + NoiseCommon.PRIME_Y;
		
		double xf0 = MathUtil.lerp(NoiseCommon.gradCoord(seed, x0, y0, xd0, yd0), NoiseCommon.gradCoord(seed, x1, y0, xd1, yd0), xs);
		double xf1 = MathUtil.lerp(NoiseCommon.gradCoord(seed, x0, y1, xd0, yd1), NoiseCommon.gradCoord(seed, x1, y1, xd1, yd1), xs);
		
		return MathUtil.lerp(xf0, xf1, ys) * 1.4247691104677813;
	}
	
	@Override
	public double _noiseImpl(int seed, double x, double y, double z)
	{
		int x0 = MathUtil.fastFloor(x);
		int y0 = MathUtil.fastFloor(y);
		int z0 = MathUtil.fastFloor(z);
		
		double xd0 = x - x0;
		double yd0 = y - y0;
		double zd0 = z - z0;
		
		double xd1 = xd0 - 1;
		double yd1 = yd0 - 1;
		double zd1 = zd0 - 1;
		
		double xs = interpQuintic(xd0);
		double ys = interpQuintic(yd0);
		double zs = interpQuintic(zd0);
		
		x0 *= NoiseCommon.PRIME_X;
		y0 *= NoiseCommon.PRIME_Y;
		z0 *= NoiseCommon.PRIME_Z;
		
		int x1 = x0 + NoiseCommon.PRIME_X;
		int y1 = y0 + NoiseCommon.PRIME_Y;
		int z1 = z0 + NoiseCommon.PRIME_Z;
		
		double xf00 = MathUtil.lerp(NoiseCommon.gradCoord(seed, x0, y0, z0, xd0, yd0, zd0), NoiseCommon.gradCoord(seed, x1, y0, z0, xd1, yd0, zd0), xs);
		double xf10 = MathUtil.lerp(NoiseCommon.gradCoord(seed, x0, y1, z0, xd0, yd1, zd0), NoiseCommon.gradCoord(seed, x1, y1, z0, xd1, yd1, zd0), xs);
		double xf01 = MathUtil.lerp(NoiseCommon.gradCoord(seed, x0, y0, z1, xd0, yd0, zd1), NoiseCommon.gradCoord(seed, x1, y0, z1, xd1, yd0, zd1), xs);
		double xf11 = MathUtil.lerp(NoiseCommon.gradCoord(seed, x0, y1, z1, xd0, yd1, zd1), NoiseCommon.gradCoord(seed, x1, y1, z1, xd1, yd1, zd1), xs);
		
		double yf0 = MathUtil.lerp(xf00, xf10, ys);
		double yf1 = MathUtil.lerp(xf01, xf11, ys);
		
		return MathUtil.lerp(yf0, yf1, zs) * 0.964921414852142333984375;
	}
	
	private static double interpQuintic(double t)
	{
		return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
	}
}
