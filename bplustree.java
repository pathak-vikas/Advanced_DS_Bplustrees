import java.io.*;
import java.util.ArrayList;
import java.util.AbstractMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

class bplustree
{
	public static void main(String[] args) throws Exception 
	{
		
		//CREATING INPUT AND OUTPUT FILES AND SUBSEQUENT BUFFERED READERS AND WRITERS
		
		File inputFile = new File(args[0]+".txt");
		BufferedReader infile = new BufferedReader(new FileReader(inputFile));


		File outputFile = new File("output_file.txt");
		if(!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		FileWriter filewriter = new FileWriter(outputFile.getAbsoluteFile());
		PrintWriter printwriter = new PrintWriter(filewriter);

		String[] tree_initialize = infile.readLine().split("\\(|,|\\)");
		int m = Integer.parseInt(tree_initialize[1]);
		BPTree tree = new BPTree(); 
		tree.Initialize(m);//  B PLUS TREE CREATED BY EXTRACTING AND SPLITTING THE 1ST LINE ( WHICH IS THE ORDER)
		String inputLine;
		while((inputLine = infile.readLine()) != null)
		{
			String[] input_choice=inputLine.split("\\(|,|\\)");

			switch (input_choice[0])
			 {
				
				case "Insert":
				{
					tree.insert(Integer.parseInt(input_choice[1]), Double.parseDouble(input_choice[2]));
					break;
				}
				case "Delete":
				{

					tree.delete(Integer.parseInt(input_choice[1]));	
					break;
				}
				case "Search":
				 {
					// FINDING VALUES FOR A SINGLE KEY
					if (input_choice.length == 2)
					{
						String searchresult = tree.search(Integer.parseInt(input_choice[1]));
						printwriter.println(searchresult);
					} 
					// FINDING VALUE FOR A RANGE OF KEYS (BOTH RANGE INCLUSIVE)
					else 
					{
						String searchresult = tree.search(Integer.parseInt(input_choice[1]),Integer.parseInt(input_choice[2]));
						printwriter.println(searchresult);
					}
					break;
				}
				
			}


		}


		filewriter.close();
		infile.close();



	}

}

class Node 
 {
 	protected ArrayList<Integer> keys;
	protected boolean isLeafNode;
	
	public boolean isOverflowed() 
	{ 
		boolean bolo=keys.size() > 2 * BPTree.m;  
		return bolo;
	}

	public boolean isUnderflowed() 
	{
		boolean bolu=keys.size() < BPTree.m;
		return bolu;
	}

}

class IndexNode extends Node 
 {


	protected ArrayList<Node> child; 

	public IndexNode(int key, Node child0, Node child1)
	 {
		
		keys = new ArrayList<Integer>();
		keys.add(key);
		child = new ArrayList<Node>();
		child.add(child0);
		child.add(child1);
		isLeafNode = false;
	}

	public IndexNode(List<Integer> newKeys, List<Node> newChild) 
	{
		keys = new ArrayList<Integer>(newKeys);
		child = new ArrayList<Node>(newChild);
		isLeafNode = false;
	}

	//INSERTING THE NODE ENTRY IN ITS RIGHT POSTION (SORTED WAY)
	public void insertSorted(Entry<Integer, Node> e, int position)
	 {
		int key = e.getKey();
		Node TempChild = e.getValue();
		if ( keys.size()<= position) 
		{
			keys.add(key);
			child.add(TempChild);
		} else
		{
			keys.add(position, key);
			child.add(position+1, TempChild);
		}
	}

}

class LeafNode extends Node
  {
	protected ArrayList<Double> values;
	protected LeafNode next;

	public LeafNode(int key1, double value1)
	 {
		isLeafNode = true;
		keys = new ArrayList<Integer>();
		values = new ArrayList<Double>();
		keys.add(key1);
		values.add(value1);

	}

	public LeafNode(List<Integer> newKeys, List<Double> newValues)
	 {
		isLeafNode = true;
		keys = new ArrayList<Integer>(newKeys);
		values = new ArrayList<Double>(newValues);

	}

	
	// INSERTING, IN A SORTED WAY, INSIDE A NODE
	public void insertSorted(int key, Double value) 
	{
		if (keys.get(0)>key) 
		{
			keys.add(0, key);
			values.add(0, value);
		} 
		else if (keys.get(keys.size() - 1)<key) 
		{
			keys.add(key);
			values.add(value);
		} else 
		{
			ListIterator<Integer> iterator = keys.listIterator();
			while (iterator.hasNext())
			 {
				if (iterator.next() > key)
				 {
					int pos = iterator.previousIndex();
					keys.add(pos, key);
					values.add(pos, value);
					break;
				}
			}

		}
	}

}

 class BPTree 
 {

	public Node root;
	public static int m;

	public void Initialize(int m)
	{
		this.m=m;
	}
	// SEARCH FOR A SPECIFIC KEY AND RETURN THE VALUE CORRESPONDING TO THE KEY
	public String search(int key) 
	{
		
		LeafNode leaf = SearchLfNodeandKey(root, key);//SEARCH THE LEAF NODE CONTAINING KEY

		int k=0;
		while((leaf.keys.size())>k)
		{
			if (leaf.keys.get(k) == key)
			{
				return leaf.values.get(k)+""; 
			}
			k++;
		}
		return "Null"; 
	}
	// SEARCH FOR A SPECIFIC KEY PAIR RANGE  AND RETURN THE VALUE CORRESPONDING TO THE KEY RANGES
public String search(int key1, int key2)
{
			LeafNode leaf = SearchLfNodeandKey(root, key1);
		
		LeafNode temp = leaf;
		String result = new String();
		int a;
		while(temp!=null){
			for( a=0;a<temp.keys.size();a++)
			{
				if(key1<=temp.keys.get(a)&&temp.keys.get(a)<=key2)
				{
					
						result+=temp.values.get(a)+",";
						
				}
				
			}
			temp =temp.next;
		}
		//removing the last comma
		result=(result.isEmpty())?"Null":result.substring(0, result.length() - 1);

		return result;

	}

	// INSERTION FUNCTION FOR (KEY,VALUE) IN BPTree
	
	public void insert(int key, double value) 
	{
		// First Insert
		if (root == null)
		{
			root = new LeafNode(key, value);
		}
		
		Entry<Integer, Node> overflowed = insertHelper(root, key, value);
		if (overflowed != null)
		{
			root = new IndexNode(overflowed.getKey(), root, overflowed.getValue());
		}

	}
	private Entry<Integer, Node> insertHelper(Node node, int key, double value)
	{
		Entry<Integer,Node> overflow = null; 
		if (node.isLeafNode)
		{
			LeafNode leaf = (LeafNode) node; 
			leaf.insertSorted(key, value);
			if (leaf.isOverflowed())
			{
				Entry<Integer, Node> rightSplit = splitLeafNode(leaf);
				return rightSplit;
			}
			return null; 
		}
		else 
		 {
			IndexNode indxNode = (IndexNode) node; 
			if (node.keys.get(0)>key ) 
				overflow = insertHelper(indxNode.child.get(0), key, value);
			else if (node.keys.get(indxNode.keys.size() - 1)<=key)
				overflow = insertHelper(indxNode.child.get(indxNode.child.size() - 1), key, value); 
			else
			 {
				
					int i=0;
					while(i < indxNode.child.size())
					{
						if (indxNode.keys.get(i) > key)
						{
						overflow = insertHelper(indxNode.child.get(i), key, value);//INSERT ONE MIDDLE CHILD
						break;
						}
						i++;
					}

			}
		}
		if (overflow != null)
		{
			IndexNode indxNode = (IndexNode)node;
			
			// figure out where to put insert the overflowed position
			int splitKey = overflow.getKey();
			int indxAtParent = indxNode.keys.size();
			if (splitKey < indxNode.keys.get(0))
			{
				indxAtParent = 0; //Finding location of overflowed index to insert 
			}
			else if (splitKey > indxNode.keys.get(indxNode.keys.size()-1))
			
			{
				indxAtParent = indxNode.child.size(); 
			} 

			else 
			{
				int i=0;
						{
						indxAtParent = i;
						}		

						i++;
					}

			
			}
			
			indxNode.insertSorted(overflow, indxAtParent);
			if (indxNode.isOverflowed())
			{
				Entry<Integer, Node> rightSplit = splitIndexNode(indxNode);
				return rightSplit;
			}
			return null;
		}
		return overflow;
		
	}

	

	public Entry<Integer, Node> splitIndexNode(IndexNode position)//split an indexNode and return the new right node and the splitted
	 {
  		ArrayList<Integer> rightKeys = new ArrayList<Integer>(m); 
		ArrayList<Node> rightChildren = new ArrayList<Node>(m+1);
		
		rightKeys.addAll(position.keys.subList(m+1, position.keys.size()));
		rightChildren.addAll(position.child.subList(m+1, position.child.size())); 
		
		
		IndexNode rightNode = new IndexNode(rightKeys, rightChildren);// push up the new position
		AbstractMap.SimpleEntry<Integer, Node> splitted = new AbstractMap.SimpleEntry<Integer, Node>(position.keys.get(m), rightNode);

		
		position.keys.subList(m, position.keys.size()).clear();// delete the right side from the left
		position.child.subList(m+1, position.child.size()).clear();
		
		return splitted;
	}
	 
	public Entry<Integer, Node> splitLeafNode(LeafNode leaf)//Split a leaf node and return the new right node and the splitting key
	 {
		 
		ArrayList<Integer> rightKeys = new ArrayList<Integer>(m+1); 
		ArrayList<Double> rightValues = new ArrayList<Double>(m+1);
		
		rightKeys.addAll(leaf.keys.subList(m, leaf.keys.size()));
		rightValues.addAll(leaf.values.subList(m, leaf.values.size())); 
	
		// delete the right side from the left
		leaf.keys.subList(m, leaf.keys.size()).clear();
		leaf.values.subList(m, leaf.values.size()).clear();
		
		LeafNode rightLeaf = new LeafNode(rightKeys, rightValues);
		
		
		ResortSibPointers(leaf, rightLeaf);// manage the new sibling

		return new AbstractMap.SimpleEntry<Integer, Node>(rightLeaf.keys.get(0), rightLeaf);

	}

	private void ResortSibPointers(LeafNode leftLeaf, LeafNode rightLeaf)// Rearranges pointers for the leftLeaf's next pointer
	 {
		if (leftLeaf.next != null)
		{
			rightLeaf.next = leftLeaf.next;
		}
		leftLeaf.next = rightLeaf; 	
	}

	private int deleteHelper(IndexNode parent, Node node, int key) { //Helper to delete() method
		int indxforDel = -1; 
		
		// find position of node in parent
		int indxInParent = -1; 
		if (parent != null)
		{
			for (indxInParent = 0; indxInParent < parent.child.size(); indxInParent++){
				if (parent.child.get(indxInParent) == node){
					break; 
				}
			}
		}
		
		if (node.isLeafNode){
			LeafNode leafNode = (LeafNode) node; 
			for (int i = 0; i < leafNode.keys.size(); i++)
			{
				if (leafNode.keys.get(i) == key){
					// delete key from leafNode's keys
					leafNode.keys.remove(i); 
					// delete the associated value
					leafNode.values.remove(i); 
					break;
				}
			}

			
			// check for underflow
			if (leafNode.isUnderflowed() && leafNode != root)
			{
				// find position leafnode resides in parent
				if (indxInParent - 1 >= 0){
					// node has left child
					LeafNode left = (LeafNode) parent.child.get(indxInParent -1);
					return manageLeafNodeUnderflow(left, leafNode, parent);
				} else {
					// node does not have left child
					LeafNode right = (LeafNode) parent.child.get(indxInParent + 1); 
					return manageLeafNodeUnderflow(leafNode, right, parent);
				}
			} else
			 {
				if (leafNode.keys.size() > 0)// may need to update parents / ancestors if deleted splitting key
					UpdateIndxNodeKeyandKey(root, key, leafNode.keys.get(0));
				return -1; // incase delete did not cause underflow
			}
			
		} 
		
		else 
		{
			IndexNode indxNode = (IndexNode) node; // node is an indexnode 
			if (key < indxNode.keys.get(0))
			{
				indxforDel = deleteHelper(indxNode, indxNode.child.get(0), key);// go down first child
			}
			else if (key >= indxNode.keys.get(indxNode.keys.size() - 1))
			{
				
				indxforDel = deleteHelper(indxNode, indxNode.child.get(indxNode.child.size() - 1), key);// go down last child
			}
			else
			 {	
				int i = 0; 	
				while(i < indxNode.keys.size())// go down the middle child
				{
					if (indxNode.keys.get(i) > key)
					{
						indxforDel = deleteHelper(indxNode, indxNode.child.get(i), key);
					}
					i++;
				}
			}
		}
		
		
		if (indxforDel != -1)// check if there's any position left to delete remaining
		{
			if (node == root )
			{
				return indxforDel; 
			}
			node.keys.remove(indxforDel);
			
			 
			if (node.isUnderflowed())//check if removal caused underflow
			{
				IndexNode left = (IndexNode)node; // if node has left sibling
				IndexNode right = (IndexNode)node; 
				if (indxInParent - 1 >= 0)// check if indexnode has sibling
				{
					left = (IndexNode) parent.child.get(indxInParent - 1);  
				} else 
				{
					right = (IndexNode) parent.child.get(indxInParent + 1);  
				}
				return manageIndexNodeUnderflow(left, right, parent);  
			}
		}
		
		return -1; 
	}
	
	public void delete(int key) {// for deleting a key/value from tree
		int position = deleteHelper(null, root, key);
		if (position != -1){
			root.keys.remove(position);
			if (root.keys.size() == 0){
				root = ((IndexNode) root).child.get(0);
			}
		}
		
		if (root.keys.size() == 0){
			root = null;
		}
	}
 
	public int manageLeafNodeUnderflow(LeafNode left, LeafNode right,IndexNode parent)// Manages leafNode underflow (merge or redistribution)
			 { 
		
		 int LRKeysSizeSum=left.keys.size() + right.keys.size();
		if (LRKeysSizeSum < 2*m)// merge
		{
			left.keys.addAll(right.keys); 
			left.values.addAll(right.values);
			left.next = right.next;
			int indxInParent = parent.child.indexOf(right);// delete the other node
			parent.child.remove(indxInParent);
			return indxInParent -1; 
		}
		
		
		int childsIndxInParent;// redistribute
		if (left.isUnderflowed())
		{
			childsIndxInParent = parent.child.indexOf(right);
			left.insertSorted(right.keys.remove(0), right.values.remove(0));// get the minimum key value of right
		} else 
		{
			childsIndxInParent = parent.child.indexOf(right);
			right.insertSorted(left.keys.remove(left.keys.size()-1), left.values.remove(left.values.size()-1));// get maximum key value of left
			parent.keys.set(childsIndxInParent - 1, parent.child.get(childsIndxInParent).keys.get(0));
		}
		parent.keys.set(childsIndxInParent - 1, parent.child.get(childsIndxInParent).keys.get(0));// update the parent's position key
		return -1;

	}

	
	public int manageIndexNodeUnderflow(IndexNode leftIndex,IndexNode rightIndex, IndexNode parent) //Manages indexnode underflow (redistribution or merge)
	
	{
		int separatingKey;
		int position=0; 
		while(position < parent.keys.size())// find separating key value from parent 
		{
			if (parent.child.get(position) == leftIndex && parent.child.get(position+1) == rightIndex)
			{
				break; 
			}
			position++;
		}
		
		separatingKey = parent.keys.get(position);
		 int LRKeysSizeSum=leftIndex.keys.size() + rightIndex.keys.size();
		
		if ( LRKeysSizeSum < 2*m)// merge action
		{
			leftIndex.keys.add(separatingKey); // move separating key down
			leftIndex.keys.addAll(rightIndex.keys);
			leftIndex.child.addAll(rightIndex.child);
			parent.child.remove(parent.child.indexOf(rightIndex));// delete the right side
			return position; 
		
		}

		
		if (leftIndex.isUnderflowed())// distribute action
		{
			leftIndex.keys.add(separatingKey);// move separating key down to leftIndex
			parent.keys.set(position, rightIndex.keys.remove(0)); // move leftmost key from right up 
			leftIndex.child.add(rightIndex.child.remove(0));// leftmost child of right is now left's
		}
		else if (rightIndex.isUnderflowed()) // move separating key down to rightIndex
		{
			rightIndex.keys.add(0, separatingKey); // the last child of left position sibling is now at right position's
			Node lastChild = leftIndex.child.remove(leftIndex.child.size() - 1);
			rightIndex.child.add(0, lastChild); // move rightmost key from leftIndex up
			parent.keys.set(parent.keys.size()-1, leftIndex.keys.remove(leftIndex.keys.size() - 1));
		}
		
		return -1;
	}

	private void UpdateIndxNodeKeyandKey(Node theNode, int searchKey, int newKey)
	{ //method used to update ancestors which could contain a deleted key
		if (theNode == null) 
			return;
		
		if (theNode.isLeafNode) 
			return; 
		
		IndexNode indxNode = (IndexNode) theNode;
		for (int i = 0; i < theNode.keys.size(); i++) //Finds the indexNode with a particular key.
		{
			
			
			if (indxNode.keys.get(i) > searchKey)
			{
				break; // not found here so we need not to go further
			}
			
			if (indxNode.keys.get(i) == searchKey)
			{
				indxNode.keys.set(i, newKey);
				return;
			}
		}
		
		
		if (searchKey < indxNode.keys.get(0))// can't find here maybe in another child
		{
			UpdateIndxNodeKeyandKey(indxNode.child.get(0), searchKey, newKey); 
		} else if (searchKey > indxNode.keys.get(indxNode.keys.size() - 1)){
			UpdateIndxNodeKeyandKey(indxNode.child.get(indxNode.child.size() - 1), searchKey, newKey);
		} else {
			for (int i = 0; i < theNode.keys.size(); i++)
			{
				if (indxNode.keys.get(i) > searchKey)
				{
					UpdateIndxNodeKeyandKey(indxNode.child.get(i), searchKey, newKey); 
				}
			}
			
		}
	}
	
	//Finds the LeafNode the key is to be inserted to
	
	private LeafNode SearchLfNodeandKey(Node theNode, int key){
		if (theNode == null)
			return null; 
		
		if (theNode.isLeafNode){
			
			return (LeafNode) theNode;
		}
		else {
			// The node is an position node
			IndexNode indexNode = (IndexNode) theNode;
			
			if (key < theNode.keys.get(0))
			{
				return SearchLfNodeandKey(indexNode.child.get(0), key);
			}
			else if (key >= theNode.keys.get(theNode.keys.size() - 1))
			 {
				return SearchLfNodeandKey(indexNode.child.get(indexNode.child.size() - 1), key);
			}
			else {
				ListIterator<Integer> iterator = indexNode.keys.listIterator();
				while (iterator.hasNext())
				{
					if (iterator.next() > key)
					{
						return SearchLfNodeandKey(indexNode.child.get(iterator.previousIndex()), key); 
					}
				}
			}
		}
		return null;
	}
	
	
}









