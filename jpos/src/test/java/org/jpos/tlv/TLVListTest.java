/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.tlv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.junit.Before;
import org.junit.Test;

public class TLVListTest {

    static final String ISO_EXCEPT_EXCEEDS_AVAL = "BAD TLV FORMAT - tag (%x) length (%d) exceeds available data";

    static final String ISO_EXCEPT_WITHOUT_LEN  = "BAD TLV FORMAT - tag (%x) without length or value";

    static final int TEST_TAG1      = 0x64;
    static final int TEST_TAG2      = 0x46;
    static final int TEST_TAG3      = 0x03e8;

    TLVList instance;

    @Before
    public void beforeTest() {
        instance = new TLVList();
    }

    @Test
    public void testAppend() {
        instance.append(TEST_TAG1, new byte[3]);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testAppend1() {
        instance.append(new TLVMsg(TEST_TAG1, new byte[0]));
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testConstructor() {
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByIndex() {
        instance.append(TEST_TAG1, new byte[0]);
        instance.deleteByIndex(0);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteByIndexThrowsIndexOutOfBounds() {
        instance.deleteByIndex(TEST_TAG1);
    }

    @Test
    public void testDeleteByTag() {
        instance.append(TEST_TAG1, new byte[1]);
        instance.append(TEST_TAG2, new byte[0]);
        instance.deleteByTag(TEST_TAG1);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByTag1() {
        instance.append(TEST_TAG1, new byte[0]);
        instance.deleteByTag(TEST_TAG3);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByTag2() {
        instance.deleteByTag(TEST_TAG1);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testDeleteByTag3() {
        instance.append(TEST_TAG2, new byte[0]);
        instance.deleteByTag(TEST_TAG2);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteByTagThrowsNullPointerException() {
        instance.append(TEST_TAG1, new byte[1]);
        instance.append(null);
        try {
            instance.deleteByTag(TEST_TAG3);
        } catch (RuntimeException ex) {
            assertFalse(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteByTagThrowsNullPointerException2() {
        instance.append(new TLVMsg());
        instance.append(TEST_TAG1, new byte[2]);
        instance.append(null);
        try {
            instance.deleteByTag(TEST_TAG1);
        } catch (RuntimeException ex) {
            assertFalse(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteByTagThrowsNullPointerException3() {
        instance.append(TEST_TAG1, new byte[1]);
        instance.append(null);
        try {
            instance.deleteByTag(TEST_TAG1);
        } catch (RuntimeException ex) {
            assertFalse(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test
    public void testTags() {
        List<TLVMsg> result = instance.getTags();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFind() {
        instance.append(TEST_TAG1, new byte[1]);
        TLVMsg expected = new TLVMsg(0x07, null);
        instance.append(expected);
        TLVMsg result = instance.find(0x07);
        assertSame(expected, result);
    }

    @Test
    public void testFind1() {
        TLVMsg expected = new TLVMsg();
        instance.append(expected);
        TLVMsg result = instance.find(0x00);
        assertSame(expected, result);
    }

    @Test
    public void testFind2() {
        instance.append(TEST_TAG3, new byte[0]);
        TLVMsg result = instance.find(TEST_TAG1);
        assertNull(result);
    }

    @Test
    public void testFind3() {
        TLVMsg result = instance.find(TEST_TAG1);
        assertNull(result);
    }

    @Test
    public void testFindIndex() {
        instance.append(TEST_TAG1, new byte[2]);
        instance.append(TEST_TAG2, new byte[2]);
        int result = instance.findIndex(TEST_TAG2);
        assertEquals(1, result);
    }

    @Test
    public void testFindIndex1() {
        instance.append(TEST_TAG3, new byte[3]);
        int result = instance.findIndex(TEST_TAG1);
        assertEquals(-1, result);
    }

    @Test
    public void testFindIndex2() {
        instance.append(new TLVMsg());
        int result = instance.findIndex(0);
        assertEquals(0, result);
    }

    @Test(expected = NullPointerException.class)
    public void testFindIndexThrowsNullPointerException() {
        instance.append(new TLVMsg());
        instance.append(null);
        instance.findIndex(TEST_TAG1);
    }

    @Test(expected = NullPointerException.class)
    public void testFindIndexThrowsNullPointerException1() {
        instance.append(null);
        instance.findIndex(TEST_TAG1);
    }

    @Test
    public void testFindNextTLV() {
        instance.findIndex(TEST_TAG1);
        instance.append(TEST_TAG3, new byte[0]);
        TLVMsg result = instance.findNextTLV();
        assertNull(result);
    }

    @Test
    public void testFindNextTLV1() {
        instance.append(TEST_TAG1, new byte[2]);
        TLVMsg expected = new TLVMsg();
        instance.append(expected);
        TLVMsg result = instance.findNextTLV();
        assertSame(expected, result);
    }

    @Test
    public void testFindNextTLV2() {
        instance.append(0x00, new byte[3]);
        TLVMsg result = instance.findNextTLV();
        assertEquals(0x00, result.getTag());
    }

    @Test
    public void testFindNextTLV3() {
        TLVMsg result = instance.findNextTLV();
        assertNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void testFindNextTLVThrowsNullPointerException() {
        instance.append(null);
        instance.findNextTLV();
    }

    @Test(expected = NullPointerException.class)
    public void testFindNextTLVThrowsNullPointerException1() {
        instance.findIndex(TEST_TAG1);
        instance.append(new TLVMsg());
        instance.append(null);
        instance.findNextTLV();
    }

    @Test(expected = NullPointerException.class)
    public void testFindThrowsNullPointerException() {
        instance.append(null);
        instance.find(TEST_TAG1);
    }

    @Test(expected = NullPointerException.class)
    public void testFindThrowsNullPointerException1() {
        instance.append(new TLVMsg());
        instance.append(null);
        instance.find(TEST_TAG1);
    }

    @Test
    public void testIndex() {
        TLVMsg expected = new TLVMsg(TEST_TAG1, null);
        instance.append(expected);
        TLVMsg result = instance.index(0);
        assertSame(expected, result);
    }

    @Test
    public void testIndex1() {
        instance.append(new TLVMsg(TEST_TAG1, "testString".getBytes()));
        instance.append(TEST_TAG1, new byte[1]);
        instance.append(new TLVMsg());
        instance.append(TEST_TAG3, new byte[3]);
        instance.append(TEST_TAG2, new byte[1]);
        instance.append(-1, new byte[0]);
        instance.deleteByIndex(0);
        instance.append(new TLVMsg());
        instance.append(1, new byte[3]);
        instance.append(null);
        instance.append(0x0a, new byte[0]);
        instance.append(new TLVMsg());
        instance.append(0x2710, new byte[2]);
        instance.append(0x0186a0, new byte[1]);
        instance.append(null);
        TLVMsg result = instance.index(12);
        assertNull(result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIndexThrowsIndexOutOfBounds() {
        instance.index(TEST_TAG1);
    }

    @Test
    public void testPack() {
        byte[] result = instance.pack();
        assertEquals(0, result.length);
    }

    @Test(expected = NullPointerException.class)
    public void testPackThrowsNullPointerException() {
        instance.append(null);
        instance.pack();
    }

    @Test
    public void testUnpack() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("030100");
        instance.unpack(buf, 0);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x03, tm.getTag());
        assertArrayEquals(ISOUtil.hex2byte("00"), tm.getValue());
    }

    @Test
    public void testUnpack1() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000100");
        instance.unpack(buf, 0);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x01, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack10() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("2080");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x20, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack11() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("030100");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x03, tm.getTag());
        assertArrayEquals(ISOUtil.hex2byte("00"), tm.getValue());
    }

    @Test
    public void testUnpack12() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("ff00");
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack2() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000100");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x01, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack3() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("0100");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
        TLVMsg tm = instance.index(0);
        assertEquals(0x01, tm.getTag());
        assertArrayEquals(new byte[0], tm.getValue());
    }

    @Test
    public void testUnpack4() throws Throwable {
        byte[] buf = new byte[0];
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack5() throws Throwable {
        byte[] buf = new byte[0];
        instance.unpack(buf, 0);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack6() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("6000");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack7() throws Throwable {
        byte[] buf = new byte[2];
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack8() throws Throwable {
        byte[] buf = new byte[3];
        instance.unpack(buf);
        assertTrue(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpack9() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("1e00");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpackWith0x00Padding() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe0000");
        instance.unpack(buf, 0);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test
    public void testUnpackWith0x00Padding1() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("878000");
        instance.unpack(buf);
        assertFalse(instance.getTags().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnpackInvalidLengthThrowsIllegalArgumentException() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("14830000");
        try {
            instance.unpack(buf);
        } catch (IllegalArgumentException ex) {
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnpackInvalidTagThrowsIllegalArgumentException() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("007f");
        try {
            instance.unpack(buf);
        } catch (IllegalArgumentException ex) {
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUnpackThrowsIndexOutOfBounds() throws Throwable {
        byte[] buf = new byte[3];
        try {
            instance.unpack(buf, 100);
        } catch (IndexOutOfBoundsException ex) {
            assertNull(ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("00ff80");
        try {
            instance.unpack(buf, 0);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x80), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException1() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("001e");
        try {
            instance.unpack(buf, 0);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x1e), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException10() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("7f0007");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0x7f00, 0x07), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException11() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("ff1e");
        try {
            instance.unpack(buf, 0);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x1e), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException12() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe2000");
        try {
            instance.unpack(buf, 0);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xfe, 0x20), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException13() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000008");
        try {
            instance.unpack(buf, 0);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x08), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException14() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("7f00");
        try {
            instance.unpack(buf, 0);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x7f00), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException15() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("f8");
        try {
            instance.unpack(buf, 0);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0xf8), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException16() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe81ed");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xfe, 0xed), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException17() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("000001");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x01), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException18() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("878009");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x09), ex.getMessage());
            assertFalse(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException2() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("0001");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x01), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException3() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe00ed");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0xed), ex.getMessage());
            assertFalse(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException4() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("00fe");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0xfe), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException5() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("fe7600");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xfe, 0x76), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException6() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("ff1e");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x1e), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException7() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("01");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x01), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException8() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("7f00");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_WITHOUT_LEN, 0x7f00), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = ISOException.class)
    public void testUnpackThrowsISOException9() throws Throwable {
        byte[] buf = ISOUtil.hex2byte("00f801");
        try {
            instance.unpack(buf);
        } catch (ISOException ex) {
            assertEquals(String.format(ISO_EXCEPT_EXCEEDS_AVAL, 0xf8, 0x01), ex.getMessage());
            assertTrue(instance.getTags().isEmpty());
            throw ex;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testUnpackThrowsNullPointerException() throws Throwable {
        instance.unpack(null, 100);
    }

    @Test(expected = NullPointerException.class)
    public void testUnpackThrowsNullPointerException1() throws Throwable {
        instance.unpack(null);
    }

}
