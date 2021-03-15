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

public class SingleOpenSimplex2SNoiseGenerator implements INoiseSource
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
		// 2D OpenSimplex2S case is a modified 2D simplex noise.
		
		final double G2 = (3.0 - NoiseCommon.SQRT3) / 6.0;
		
		int i = MathUtil.fastFloor(x);
		int j = MathUtil.fastFloor(y);
		
		double xi = x - i;
		double yi = y - j;
		
		i *= NoiseCommon.PRIME_X;
		j *= NoiseCommon.PRIME_Y;
		
		int i1 = i + NoiseCommon.PRIME_X;
		int j1 = j + NoiseCommon.PRIME_Y;
		
		double t = (xi + yi) * G2;
		
		double x0 = xi - t;
		double y0 = yi - t;
		
		double a0 = (2.0 / 3.0) - x0 * x0 - y0 * y0;
		
		double value = (a0 * a0) * (a0 * a0) * NoiseCommon.gradCoord(seed, i, j, x0, y0);
		
		double a1 = 2.0 * (1.0 - 2.0 * G2) * (1.0 / G2 - 2.0) * t + (-2.0 * (1.0 - 2.0 * G2) * (1.0 - 2.0 * G2) + a0);
		double x1 = x0 - (1.0 - 2.0 * G2);
		double y1 = y0 - (1.0 - 2.0 * G2);
		
		value += (a1 * a1) * (a1 * a1) * NoiseCommon.gradCoord(seed, i1, j1, x1, y1);
		
		// Nested conditionals were faster than compact bit logic/arithmetic.
		double xmyi = xi - yi;
		
		if(t > G2)
		{
			if(xi + xmyi > 1.0)
			{
				double x2 = x0 + (3.0 * G2 - 2.0);
				double y2 = y0 + (3.0 * G2 - 1.0);
				double a2 = (2.0 / 3.0) - x2 * x2 - y2 * y2;
				
				if(a2 > 0) value += (a2 * a2) * (a2 * a2) * NoiseCommon.gradCoord(seed, i + (NoiseCommon.PRIME_X << 1), j + NoiseCommon.PRIME_Y, x2, y2);
			}
			else
			{
				double x2 = x0 + G2;
				double y2 = y0 + (G2 - 1.0);
				double a2 = (2.0 / 3.0) - x2 * x2 - y2 * y2;
				
				if(a2 > 0) value += (a2 * a2) * (a2 * a2) * NoiseCommon.gradCoord(seed, i, j + NoiseCommon.PRIME_Y, x2, y2);
			}
			
			if(yi - xmyi > 1.0)
			{
				double x3 = x0 + (3.0 * G2 - 1.0);
				double y3 = y0 + (3.0 * G2 - 2.0);
				double a3 = (2.0 / 3.0) - x3 * x3 - y3 * y3;
				
				if(a3 > 0) value += (a3 * a3) * (a3 * a3) * NoiseCommon.gradCoord(seed, i + NoiseCommon.PRIME_X, j + (NoiseCommon.PRIME_Y << 1), x3, y3);
			}
			else
			{
				double x3 = x0 + (G2 - 1.0);
				double y3 = y0 + G2;
				double a3 = (2.0 / 3.0) - x3 * x3 - y3 * y3;
				
				if(a3 > 0) value += (a3 * a3) * (a3 * a3) * NoiseCommon.gradCoord(seed, i + NoiseCommon.PRIME_X, j, x3, y3);
			}
		}
		else
		{
			if(xi + xmyi < 0.0)
			{
				double x2 = x0 + (1.0 - G2);
				double y2 = y0 - G2;
				double a2 = (2.0 / 3.0) - x2 * x2 - y2 * y2;
				
				if(a2 > 0) value += (a2 * a2) * (a2 * a2) * NoiseCommon.gradCoord(seed, i - NoiseCommon.PRIME_X, j, x2, y2);
			}
			else
			{
				double x2 = x0 + (G2 - 1.0);
				double y2 = y0 + G2;
				double a2 = (2.0 / 3.0) - x2 * x2 - y2 * y2;
				
				if(a2 > 0) value += (a2 * a2) * (a2 * a2) * NoiseCommon.gradCoord(seed, i + NoiseCommon.PRIME_X, j, x2, y2);
			}
			
			if(yi < xmyi)
			{
				double x2 = x0 - G2;
				double y2 = y0 - (G2 - 1.0);
				double a2 = (2.0 / 3.0) - x2 * x2 - y2 * y2;
				
				if(a2 > 0) value += (a2 * a2) * (a2 * a2) * NoiseCommon.gradCoord(seed, i, j - NoiseCommon.PRIME_Y, x2, y2);
			}
			else
			{
				double x2 = x0 + G2;
				double y2 = y0 + (G2 - 1.0);
				double a2 = (2.0 / 3.0) - x2 * x2 - y2 * y2;
				
				if(a2 > 0) value += (a2 * a2) * (a2 * a2) * NoiseCommon.gradCoord(seed, i, j + NoiseCommon.PRIME_Y, x2, y2);
			}
		}
		
		return value * 18.24196194486065;
	}
	
	@Override
	public double _noiseImpl(int seed, double x, double y, double z)
	{
		// 3D OpenSimplex2S case uses two offset rotated cube grids.
		
		int i = MathUtil.fastFloor(x);
		int j = MathUtil.fastFloor(y);
		int k = MathUtil.fastFloor(z);
		
		double xi = x - i;
		double yi = y - j;
		double zi = z - k;
		
		i *= NoiseCommon.PRIME_X;
		j *= NoiseCommon.PRIME_Y;
		k *= NoiseCommon.PRIME_Z;
		
		int seed2 = seed + 1293373;
		
		int xNMask = (int)(-0.5 - xi);
		int yNMask = (int)(-0.5 - yi);
		int zNMask = (int)(-0.5 - zi);
		
		double x0 = xi + xNMask;
		double y0 = yi + yNMask;
		double z0 = zi + zNMask;
		
		double a0 = 0.75 - x0 * x0 - y0 * y0 - z0 * z0;
		
		double value = (a0 * a0) * (a0 * a0) * NoiseCommon.gradCoord(seed, i + (xNMask & NoiseCommon.PRIME_X), j + (yNMask & NoiseCommon.PRIME_Y), k + (zNMask & NoiseCommon.PRIME_Z), x0, y0, z0);
		
		double x1 = xi - 0.5;
		double y1 = yi - 0.5;
		double z1 = zi - 0.5;
		
		double a1 = 0.75 - x1 * x1 - y1 * y1 - z1 * z1;
		
		value += (a1 * a1) * (a1 * a1) * NoiseCommon.gradCoord(seed2, i + NoiseCommon.PRIME_X, j + NoiseCommon.PRIME_Y, k + NoiseCommon.PRIME_Z, x1, y1, z1);
		
		double xAFlipMask0 = ((xNMask | 1) << 1) * x1;
		double yAFlipMask0 = ((yNMask | 1) << 1) * y1;
		double zAFlipMask0 = ((zNMask | 1) << 1) * z1;
		
		double xAFlipMask1 = (-2 - (xNMask << 2)) * x1 - 1.0;
		double yAFlipMask1 = (-2 - (yNMask << 2)) * y1 - 1.0;
		double zAFlipMask1 = (-2 - (zNMask << 2)) * z1 - 1.0;
		
		boolean skip5 = false;
		
		double a2 = xAFlipMask0 + a0;
		
		if(a2 > 0.0)
		{
			double x2 = x0 - (xNMask | 1);
			double y2 = y0;
			double z2 = z0;
			
			value += (a2 * a2) * (a2 * a2) * NoiseCommon.gradCoord(seed, i + (~xNMask & NoiseCommon.PRIME_X), j + (yNMask & NoiseCommon.PRIME_Y), k + (zNMask & NoiseCommon.PRIME_Z), x2, y2, z2);
		}
		else
		{
			double a3 = yAFlipMask0 + zAFlipMask0 + a0;
			
			if(a3 > 0.0)
			{
				double x3 = x0;
				double y3 = y0 - (yNMask | 1);
				double z3 = z0 - (zNMask | 1);
				
				value += (a3 * a3) * (a3 * a3) * NoiseCommon.gradCoord(seed, i + (xNMask & NoiseCommon.PRIME_X), j + (~yNMask & NoiseCommon.PRIME_Y), k + (~zNMask & NoiseCommon.PRIME_Z), x3, y3, z3);
			}
			
			double a4 = xAFlipMask1 + a1;
			
			if(a4 > 0.0)
			{
				double x4 = (xNMask | 1) + x1;
				double y4 = y1;
				double z4 = z1;
				
				value += (a4 * a4) * (a4 * a4) * NoiseCommon.gradCoord(seed2, i + (xNMask & (NoiseCommon.PRIME_X * 2)), j + NoiseCommon.PRIME_Y, k + NoiseCommon.PRIME_Z, x4, y4, z4);
				
				skip5 = true;
			}
		}
		
		boolean skip9 = false;
		
		double a6 = yAFlipMask0 + a0;
		
		if(a6 > 0.0)
		{
			double x6 = x0;
			double y6 = y0 - (yNMask | 1);
			double z6 = z0;
			
			value += (a6 * a6) * (a6 * a6) * NoiseCommon.gradCoord(seed, i + (xNMask & NoiseCommon.PRIME_X), j + (~yNMask & NoiseCommon.PRIME_Y), k + (zNMask & NoiseCommon.PRIME_Z), x6, y6, z6);
		}
		else
		{
			double a7 = xAFlipMask0 + zAFlipMask0 + a0;
			
			if(a7 > 0.0)
			{
				double x7 = x0 - (xNMask | 1);
				double y7 = y0;
				double z7 = z0 - (zNMask | 1);
				
				value += (a7 * a7) * (a7 * a7) * NoiseCommon.gradCoord(seed, i + (~xNMask & NoiseCommon.PRIME_X), j + (yNMask & NoiseCommon.PRIME_Y), k + (~zNMask & NoiseCommon.PRIME_Z), x7, y7, z7);
			}
			
			double a8 = yAFlipMask1 + a1;
			
			if(a8 > 0.0)
			{
				double x8 = x1;
				double y8 = (yNMask | 1) + y1;
				double z8 = z1;
				
				value += (a8 * a8) * (a8 * a8) * NoiseCommon.gradCoord(seed2, i + NoiseCommon.PRIME_X, j + (yNMask & (NoiseCommon.PRIME_Y << 1)), k + NoiseCommon.PRIME_Z, x8, y8, z8);
				
				skip9 = true;
			}
		}
		
		boolean skipD = false;
		
		double aA = zAFlipMask0 + a0;
		
		if(aA > 0.0)
		{
			double xA = x0;
			double yA = y0;
			double zA = z0 - (zNMask | 1);
			
			value += (aA * aA) * (aA * aA) * NoiseCommon.gradCoord(seed, i + (xNMask & NoiseCommon.PRIME_X), j + (yNMask & NoiseCommon.PRIME_Y), k + (~zNMask & NoiseCommon.PRIME_Z), xA, yA, zA);
		}
		else
		{
			double aB = xAFlipMask0 + yAFlipMask0 + a0;
			
			if(aB > 0.0)
			{
				double xB = x0 - (xNMask | 1);
				double yB = y0 - (yNMask | 1);
				double zB = z0;
				
				value += (aB * aB) * (aB * aB) * NoiseCommon.gradCoord(seed, i + (~xNMask & NoiseCommon.PRIME_X), j + (~yNMask & NoiseCommon.PRIME_Y), k + (zNMask & NoiseCommon.PRIME_Z), xB, yB, zB);
			}
			
			double aC = zAFlipMask1 + a1;
			
			if(aC > 0.0)
			{
				double xC = x1;
				double yC = y1;
				double zC = (zNMask | 1) + z1;
				
				value += (aC * aC) * (aC * aC) * NoiseCommon.gradCoord(seed2, i + NoiseCommon.PRIME_X, j + NoiseCommon.PRIME_Y, k + (zNMask & (NoiseCommon.PRIME_Z << 1)), xC, yC, zC);
				
				skipD = true;
			}
		}
		
		if(!skip5)
		{
			double a5 = yAFlipMask1 + zAFlipMask1 + a1;
			
			if(a5 > 0.0)
			{
				double x5 = x1;
				double y5 = (yNMask | 1) + y1;
				double z5 = (zNMask | 1) + z1;
				
				value += (a5 * a5) * (a5 * a5) * NoiseCommon.gradCoord(seed2, i + NoiseCommon.PRIME_X, j + (yNMask & (NoiseCommon.PRIME_Y << 1)), k + (zNMask & (NoiseCommon.PRIME_Z << 1)), x5, y5, z5);
			}
		}
		
		if(!skip9)
		{
			double a9 = xAFlipMask1 + zAFlipMask1 + a1;
			
			if(a9 > 0.0)
			{
				double x9 = (xNMask | 1) + x1;
				double y9 = y1;
				double z9 = (zNMask | 1) + z1;
				
				value += (a9 * a9) * (a9 * a9) * NoiseCommon.gradCoord(seed2, i + (xNMask & (NoiseCommon.PRIME_X * 2)), j + NoiseCommon.PRIME_Y, k + (zNMask & (NoiseCommon.PRIME_Z << 1)), x9, y9, z9);
			}
		}
		
		if(!skipD)
		{
			double aD = xAFlipMask1 + yAFlipMask1 + a1;
			
			if(aD > 0.0)
			{
				double xD = (xNMask | 1) + x1;
				double yD = (yNMask | 1) + y1;
				double zD = z1;
				
				value += (aD * aD) * (aD * aD) * NoiseCommon.gradCoord(seed2, i + (xNMask & (NoiseCommon.PRIME_X << 1)), j + (yNMask & (NoiseCommon.PRIME_Y << 1)), k + NoiseCommon.PRIME_Z, xD, yD, zD);
			}
		}
		
		return value * 9.046026385208288;
	}
}
