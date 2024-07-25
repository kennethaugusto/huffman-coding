package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */

    public void makeSortedList() {
        StdIn.setFile(fileName);
        // create array to store frequency
        int[] frequency = new int[128];
        // count characters
        int count = 0;

        // go through each character in file
        while (StdIn.hasNextChar()) {
            // read next char
            char c = StdIn.readChar();
            // keep track of frequency and count
            frequency[c] += 1;
            count += 1;
        }

        // case: only one char in input file
        if (count == 1) {
            // add fake character
            frequency[(int) 'a'] += 1;
            frequency[(int) 'b'] += 1;
            count += 2;
        }

        // store character frequencies
        sortedCharFreqList = new ArrayList<CharFreq>();

        // go thru frequency array
        for (int i = 0; i < frequency.length; i++) {
            if (frequency[i] > 0) {
                // if frequency greater than 0, add new CharFreq object to list
                double probOcc = (double) frequency[i] / count;
                sortedCharFreqList.add(new CharFreq((char) i, probOcc));
            }
        }

        // sort list in ascending order
        Collections.sort(sortedCharFreqList);

    }
    

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {

        // initialize two queues 
        Queue<TreeNode> source = new Queue<TreeNode>();
        Queue<TreeNode> target = new Queue<TreeNode>();
        
        // create a node for each character in sortedCharFreqList and enqueue into source
        for (int i = 0; i < sortedCharFreqList.size(); i++) {
            TreeNode newNode = new TreeNode();
            newNode.setData(sortedCharFreqList.get(i));
            source.enqueue(newNode);
        }

        while (!source.isEmpty() || target.size() > 1) {
            TreeNode left, right;

            // if target queue is empty, dequeue two nodes from source queue and create a parent node
            if (target.isEmpty()) {
                left = source.dequeue();
                right = source.dequeue();
                CharFreq abc = new CharFreq('\0', left.getData().getProbOcc() + right.getData().getProbOcc());
                TreeNode huffmanRoot = new TreeNode(abc, left, right);
                target.enqueue(huffmanRoot);
            }

            // if target queue is not empty, dequeue nodes from both queues based on their frequency of occurrence
            else {
                // dequeue node from source or target queue depending on frequency of occurrence
                if (source.isEmpty() || source.peek().getData().getProbOcc() > target.peek().getData().getProbOcc()) {
                    left = target.dequeue();
                } 
                else {
                    left = source.dequeue();
                }
                if (source.isEmpty() || source.peek().getData().getProbOcc() > target.peek().getData().getProbOcc()) {
                    right = target.dequeue();
                }
                else {
                    right = source.dequeue();
                }
                // create parent node by adding probOcc of left and right nodes
                CharFreq def = new CharFreq('\0', left.getData().getProbOcc() + right.getData().getProbOcc());
                TreeNode huffmanRoot = new TreeNode(def, left, right);
                target.enqueue(huffmanRoot);
            }
        }

        // huffman tree constructed and stored in last node of target queue
        huffmanRoot = target.dequeue();
	    
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {

        // create array to store encodings for all characters
        encodings = new String[128];
        // call helper method for huffmanRoot node and empty string
        helpEncode(huffmanRoot, "");
    
    }

    // helper method, take in TreeNode node and String encoding
    private void helpEncode(TreeNode node, String encoding) {
        // if node is null, return and exit recursion
        if (node == null) {
            return;
        }

        // if current node is leaf node, add its encoding to encodings array and return
        if (node.getLeft() == null && node.getRight() == null) {
            encodings[(int) node.getData().getCharacter()] = encoding;
            return;
        }
        
        // if current node is not a leaf node, call generateEncodings for its left and right children
        helpEncode(node.getLeft(), encoding + "0");
        helpEncode(node.getRight(), encoding + "1");
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        // store encoded string
        String encodedString = "";
        // loop through input file
        while (StdIn.hasNextChar()) {
            char c = StdIn.readChar();
            encodedString += encodings[(int) c];
        }
        writeBitString(encodedFile, encodedString);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
        // read encodedFile
        String bitString = readBitString(encodedFile);
        // initialize node to root of tree
        TreeNode cNode = huffmanRoot;
        // initialize an empty string to hold decoded characters
        String decodedString = "";
        // iterate thru each bit in bit string
        for (int i = 0; i < bitString.length(); i++) {
            // get current bit
            char bit = bitString.charAt(i);
            // update current node based on the bit
            if (bit == '0') {
                cNode = cNode.getLeft();
            } 
            else {
                cNode = cNode.getRight();
            }
            // if current node is a leaf node, append charcacter to decoded string and reset current node to the root
            if (cNode.getLeft() == null && cNode.getRight() == null) {
                decodedString += cNode.getData().getCharacter();
                cNode = huffmanRoot;
            }
        }
        StdOut.print(decodedString.toString());
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
