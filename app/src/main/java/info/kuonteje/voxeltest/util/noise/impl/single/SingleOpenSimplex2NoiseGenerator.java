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

public class SingleOpenSimplex2NoiseGenerator implements INoiseSource
{
	@Override
	public double _transform2d(int seed, double x, double y, _IPassthrough2d passthrough)
	{
		final double F2 = 0.5 * (NoiseCommon.SQRT3 - 1.0);
		
		double t = (x + y) * F2;
		return passthrough.apply(seed, x + t, y + t);
	}
	
	@Override
	public double _transform3d(int seed, double x, double y, double z, _IPassthrough3d passthrough)
	{
		final double R3 = 2.0 / 3.0;
		
		double r = (x + y + z) * R3; // Rotation, not skew
		return passthrough.apply(seed, r - x, r - y, r - z);
	}
	
	@Override
	public double noise(int seed, double x, double y)
	{
		return _transform2d(seed, x, y, this::_noiseImpl);
	}
	
	@Override
	public double noise(int seed, double x, double y, double z)
	{
		return _transform3d(seed, x, y, z, this::_noiseImpl);
	}
	
	@Override
	public double _noiseImpl(int seed, double x, double y)
	{
		// 2D OpenSimplex2 case uses the same algorithm as ordinary Simplex.
		
		final double G2 = (3.0 - NoiseCommon.SQRT3) / 6.0;
		
		int i = MathUtil.fastFloor(x);
		int j = MathUtil.fastFloor(y);
		
		double xi = x - i;
		double yi = y - j;
		
		double t = (xi + yi) * G2;
		
		double x0 = xi - t;
		double y0 = yi - t;
		
		i *= NoiseCommon.PRIME_X;
		j *= NoiseCommon.PRIME_Y;
		
		double n0, n1, n2;
		
		double a = 0.5 - x0 * x0 - y0 * y0;
		
		if(a <= 0) n0 = 0;
		else n0 = (a * a) * (a * a) * NoiseCommon.gradCoord(seed, i, j, x0, y0);
		
		double c = 2 * (1.0 - 2.0 * G2) * (1.0 / G2 - 2.0) * t + (-2.0 * (1.0 - 2.0 * G2) * (1.0 - 2.0 * G2) + a);
		if(c <= 0) n2 = 0;
		else
		{
			double x2 = x0 + (2.0 * G2 - 1.0);
			double y2 = y0 + (2.0 * G2 - 1.0);
			
			n2 = (c * c) * (c * c) * NoiseCommon.gradCoord(seed, i + NoiseCommon.PRIME_X, j + NoiseCommon.PRIME_Y, x2, y2);
		}
		
		if(y0 > x0)
		{
			double x1 = x0 + G2;
			double y1 = y0 + (G2 - 1.0);
			double b = 0.5 - x1 * x1 - y1 * y1;
			
			if(b <= 0) n1 = 0;
			else n1 = (b * b) * (b * b) * NoiseCommon.gradCoord(seed, i, j + NoiseCommon.PRIME_Y, x1, y1);
		}
		else
		{
			double x1 = x0 + (G2 - 1.0);
			double y1 = y0 + G2;
			double b = 0.5 - x1 * x1 - y1 * y1;
			
			if(b <= 0.0) n1 = 0;
			else n1 = (b * b) * (b * b) * NoiseCommon.gradCoord(seed, i + NoiseCommon.PRIME_X, j, x1, y1);
		}
		
		return (n0 + n1 + n2) * 99.83685446303647;
	}
	
	@Override
	public double _noiseImpl(int seed, double x, double y, double z)
	{
		// 3D OpenSimplex2 case uses two offset rotated cube grids.
		
		int i = NoiseCommon.fastRound(x);
		int j = NoiseCommon.fastRound(y);
		int k = NoiseCommon.fastRound(z);
		
		double x0 = x - i;
		double y0 = y - j;
		double z0 = z - k;
		
		int xNSign = (int)(-1.0 - x0) | 1;
		int yNSign = (int)(-1.0 - y0) | 1;
		int zNSign = (int)(-1.0 - z0) | 1;
		
		double ax0 = xNSign * -x0;
		double ay0 = yNSign * -y0;
		double az0 = zNSign * -z0;
		
		i *= NoiseCommon.PRIME_X;
		j *= NoiseCommon.PRIME_Y;
		k *= NoiseCommon.PRIME_Z;
		
		double value = 0;
		double a = (0.6f - x0 * x0) - (y0 * y0 + z0 * z0);
		
		for(int l = 0;; l++)
		{
			if(a > 0) value += (a * a) * (a * a) * NoiseCommon.gradCoord(seed, i, j, k, x0, y0, z0);
			
			if(ax0 >= ay0 && ax0 >= az0)
			{
				double b = a + ax0 + ax0;
				
				if(b > 1.0)
				{
					b -= 1;
					value += (b * b) * (b * b) * NoiseCommon.gradCoord(seed, i - xNSign * NoiseCommon.PRIME_X, j, k, x0 + xNSign, y0, z0);
				}
			}
			else if(ay0 > ax0 && ay0 >= az0)
			{
				double b = a + ay0 + ay0;
				
				if(b > 1.0)
				{
					b -= 1.0;
					value += (b * b) * (b * b) * NoiseCommon.gradCoord(seed, i, j - yNSign * NoiseCommon.PRIME_Y, k, x0, y0 + yNSign, z0);
				}
			}
			else
			{
				double b = a + az0 + az0;
				
				if(b > 1.0)
				{
					b -= 1.0;
					value += (b * b) * (b * b) * NoiseCommon.gradCoord(seed, i, j, k - zNSign * NoiseCommon.PRIME_Z, x0, y0, z0 + zNSign);
				}
			}
			
			if(l == 1) break;
			
			ax0 = 0.5 - ax0;
			ay0 = 0.5 - ay0;
			az0 = 0.5 - az0;
			
			x0 = xNSign * ax0;
			y0 = yNSign * ay0;
			z0 = zNSign * az0;
			
			a += (0.75 - ax0) - (ay0 + az0);
			
			i += (xNSign >> 1) & NoiseCommon.PRIME_X;
			j += (yNSign >> 1) & NoiseCommon.PRIME_Y;
			k += (zNSign >> 1) & NoiseCommon.PRIME_Z;
			
			xNSign = -xNSign;
			yNSign = -yNSign;
			zNSign = -zNSign;
			
			seed = ~seed;
		}
		
		return value * 32.69428253173828125;
	}
}
