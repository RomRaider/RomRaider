/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.maps.checksum;

import java.util.Map;

import com.romraider.Settings.Endian;
import com.romraider.xml.RomAttributeParser;

/**
 * This class implements the E38 (GM) PCM checksum algorithm Implementation
 * ported from: https://github.com/antuspcm/E38-Sum-Tool
 */
public final class ChecksumE38PCM implements ChecksumManager {
	segment[] seg = new segment[7];

	class segment {
		public int start;
		public int end;
		public short lsum;
		public short lcvn;
		public short csum;
		public short ccvn;
		boolean calculated;
	}

	@Override
	public void configure(Map<String, String> vars) {
	}

	@Override
	public int getNumberOfChecksums() {
		return 12;
	}

	@Override
	public int validate(byte[] binData) {
		calculate(binData);

		int correctChecksums = 0;
		for (int i = 1; i <= 6; i++) {
			if (seg[i].calculated) {
				if (seg[i].ccvn == seg[i].lcvn) {
					correctChecksums++;
				}
				if (seg[i].lsum == seg[i].csum) {
					correctChecksums++;
				}
			}
		}
		return correctChecksums;
	}

	@Override
	public int update(byte[] binData) {
		int correctedChecksums = 0;
		
		// Seems like some checksums depend on each other?
		// Need to do it twice
		for (int k = 0; k < 2; k++) {
			calculate(binData);

			for (int i = 1; i <= 6; i++) {
				if (seg[i].ccvn != seg[i].lcvn) {
					correctedChecksums++;

					binData[seg[i].start + 0x1E] = (byte) ((seg[i].ccvn >>> 8) & 0xFF);
					binData[seg[i].start + 0x1E + 1] = (byte) ((seg[i].ccvn) & 0xFF);

				}
				if (seg[i].lsum != seg[i].csum) {
					correctedChecksums++;

					binData[seg[i].start] = (byte) ((seg[i].csum >>> 8) & 0xFF);
					binData[seg[i].start + 1] = (byte) ((seg[i].csum) & 0xFF);
				}
			}
		}
		return correctedChecksums;
	}

	private void calculate(byte[] bin) {
		// load index
		int index = 0x10000;
		for (int i = 1; i <= 6; i++) {
			seg[i] = new segment();
		}

		seg[1].start = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x24, 4, false);
		seg[1].end = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x28, 4, false);
		seg[2].start = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x48, 4, false);
		seg[2].end = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x4c, 4, false);
		seg[3].start = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x6b, 4, false);
		seg[3].end = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x6f, 4, false);
		seg[4].start = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x8e, 4, false);
		seg[4].end = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0x92, 4, false);
		seg[5].start = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0xb1, 4, false);
		seg[5].end = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0xb5, 4, false);
		seg[6].start = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0xd4, 4, false);
		seg[6].end = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, index + 0xd8, 4, false);

		for (int a = 1; a <= 6; a++) {
			if (seg[a].start > 0x200000) {
				// log.AppendText("Segment " + a + " start is out of range" +
				return;
			}

			if (seg[a].end > 0x200000) {
				// log.AppendText("Segment " + a + " end is out of range" +
				return;
			}

			if (seg[a].start % 2 != 0) {
				// log.AppendText("Segment " + a + " does not start on a word boundry" +
				return;
			}
			if (seg[a].end % 2 == 0) {
				// log.AppendText("Segment " + a + " does not end on a word boundry" +
				return;
			}
			if (seg[a].end - seg[a].start < 0x24) {
				// log.AppendText("Segment " + a + " is impossibly short" +
				return;
			}
		}

		// load data, log
		for (int i = 1; i <= 6; i++) {
			seg[i].lsum = (short) RomAttributeParser.parseByteValue(bin, Endian.BIG, seg[i].start, 2, false);
			seg[i].lcvn = (short) RomAttributeParser.parseByteValue(bin, Endian.BIG, seg[i].start + 0x1E, 2, false);
			seg[i].csum = segmentsum(bin, seg[i].start, seg[i].end);
			seg[i].ccvn = segmentcvn(bin, seg[i].start, seg[i].end);
			seg[i].calculated = true;
		}
	}

	private int gmcrc16(byte[] bin, long init, int s, int e) {
		int num;
		byte num2;
		byte num3;
		byte num4;
		int num5;
		int num6;

		int[] crc16t = new int[] { 0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241, 0xC601, 0x06C0,
				0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440, 0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81,
				0x0E40, 0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841, 0xD801, 0x18C0, 0x1980, 0xD941,
				0x1B00, 0xDBC1, 0xDA81, 0x1A40, 0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41, 0x1400,
				0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641, 0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1,
				0xD081, 0x1040, 0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240, 0x3600, 0xF6C1, 0xF781,
				0x3740, 0xF501, 0x35C0, 0x3480, 0xF441, 0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
				0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840, 0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01,
				0x2BC0, 0x2A80, 0xEA41, 0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40, 0xE401, 0x24C0,
				0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640, 0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080,
				0xE041, 0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240, 0x6600, 0xA6C1, 0xA781, 0x6740,
				0xA501, 0x65C0, 0x6480, 0xA441, 0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41, 0xAA01,
				0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840, 0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0,
				0x7A80, 0xBA41, 0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40, 0xB401, 0x74C0, 0x7580,
				0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640, 0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
				0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241, 0x9601, 0x56C0, 0x5780, 0x9741, 0x5500,
				0x95C1, 0x9481, 0x5440, 0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40, 0x5A00, 0x9AC1,
				0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841, 0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81,
				0x4A40, 0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41, 0x4400, 0x84C1, 0x8581, 0x4540,
				0x8701, 0x47C0, 0x4680, 0x8641, 0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040 };

		num = s; // location counter
		while ((int) num <= (int) e) // until final address (inclusive)
		{
			num2 = bin[num]; // num2=current byte
			num3 = (byte) (init & 0xff); // num3=sum low byte
			num4 = (byte) ((init & 0xff00) / 0x100); // num4=sum high byte
			int temp1 = (short) (((num3 & 0xFF)) ^ (num2 & 0xFF));
			num5 = crc16t[temp1]; // sum low byte xord with data used as index to another xor table
			int temp2 = (int) (num5 ^ (num4 & 0xFF));
			init = temp2 & 0xffff; // xor table byte xord with sum high byte
			num++; // next byte
		}
		num6 = (int) init & 0xFFFFFFFF; // return the sum
		return num6;
	}

	public int swapab(int p0) {
		int num;
		int num2;
		int num3;
		try {
			num = (p0 & 0xff00) / 0x100;
			num2 = p0 & 0xff;
			num3 = (num2 * 0x100) + num;
			return num3;
		} catch (Exception ex) {
		}
		return 0;
	}

	public short segmentsum(byte[] bin, int s, int e) {
		int sum = 0;
		for (int i = s + 2; i <= e; i += 2) {
			int combined = (int) RomAttributeParser.parseByteValue(bin, Endian.BIG, i, 2, false);
			sum += combined;
		}
		sum = (((sum & 0xFFFF) ^ 0xFFFF) + 1) & 0xFFFF;
		return (short) sum;
	}

	public short segmentcvn(byte[] bin, int s, int e) {
		int sum = gmcrc16(bin, 0, s + 2, s + 0x1d);
		sum = gmcrc16(bin, sum, s + 0x20, e);
		sum = swapab(sum);
		return (short) sum;
	}
}