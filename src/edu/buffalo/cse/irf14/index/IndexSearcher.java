package edu.buffalo.cse.irf14.index;

import java.util.ArrayList;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.query.queryOps;

public class IndexSearcher 
{
	public static DocVector DoOperation(DocVector left, DocVector right,String boolOPS)
	{
		if(left == null || right==null)
		{
			if(right == null)
			{
				return left;
			}
			else if(left == null)
			{
				return right;
			}
		}
		
		DocVector result = null;
		switch(queryOps.toOps(boolOPS))
		{
			case OR:
				result=Union(left,right);
				break;
			case AND:
				result=Intersection(left,right);
				break;
			case NOT:
				result=Negation(left, right);
				break;
			case BAD_OPS:
				System.out.println("Bad query operation given !!");
				break;
		}
		return result;
	}
	
	public static DocVector Intersection(DocVector...candidatelists)
	{
		ArrayList<Integer> Order = sort(candidatelists);
		DocVector intersectionList=candidatelists[(Order.remove(0))];
		
		while(!Order.isEmpty() && !intersectionList.isEmpty())
		{
			intersectionList = intersect(intersectionList,candidatelists[Order.remove(0)]);
		}
		
		if(intersectionList.isEmpty())
		{
			System.out.println("No result");
			return null;
		}
		checkResultVector(intersectionList);
		return intersectionList;
	}
	
	public static DocVector Negation(DocVector yes,DocVector no)
	{
		DocVector result=new DocVector();
		
		int yesIndex=0,noIndex=0;
		int yesLength=yes.size(),noLength=no.size();
		
		// if First docID of NO is > last docID of yes then return yes
		if(no.get(noIndex).docID>yes.get(yesLength-1).docID)
		{
			return yes;
		}
		
		// while we still have elements to compare
		while(yesIndex<yesLength && noIndex<noLength)
		{
			if(yes.get(yesIndex).equals(no.get(noIndex)))
			{
				yesIndex++;noIndex++;
			}
			else if(yes.get(yesIndex).docID<no.get(noIndex).docID)
			{
				result.add(yes.get(yesIndex));
				yesIndex++;
			}
			else
			{
				noIndex++;
			}
		}
		while(yesIndex<yesLength)
		{
			result.add(yes.get(yesIndex));
			yesIndex++;
		}
		return result;
	}
	
	public static DocVector Union(DocVector ...candidateLists)
	{
		// show things into a hashmap and unroll ---???
		ArrayList<Integer> Order = sort(candidateLists);
		DocVector unionVector = candidateLists[(Order.remove(0))];
		while(!Order.isEmpty() && ! unionVector.isEmpty())
		{
			unionVector = union(unionVector,candidateLists[Order.remove(0)]);
		}
		checkResultVector(unionVector);
		return unionVector;
	}
	
	private static DocVector union(DocVector parentVector, DocVector candidateVector)
	{
		
		DocVec_Element tmp;
		DocVec_Element tmp2;
		DocVector unionVector = new DocVector();
		int indexParent = 0, indexCandidate = 0;
		int parentSize=parentVector.size();
		int candidateSize=candidateVector.size();
		
		while(indexParent < parentSize && indexCandidate < candidateSize)
		{
			// we find that the 2 terms have occured in the same document
			if(parentVector.get(indexParent).equals(candidateVector.get(indexCandidate)))
			{
				// add termstats from 2nd docVector to 1st docVector --
				
				tmp = parentVector.get(indexParent);
				tmp2 = candidateVector.get(indexCandidate);
				/**
				 * store the wright_Term_Query
				 * Set the term queries weights
				 */
				/*if(parentVector.vec_type == vectorType.S)
				{
					tmp.setWTQ(parentVector.wTQ);
				}
				if(candidateVector.vec_type == vectorType.S)
				{
					tmp2.setWTQ(candidateVector.wTQ);
				}*/
				tmp.add(tmp2.statsList);
				unionVector.add(tmp);
				indexParent++;
				indexCandidate++;
			}
			else if((parentVector.get(indexParent)).docID <(candidateVector.get(indexCandidate)).docID)
			{
				tmp=parentVector.get(indexParent);
				/*if(parentVector.vec_type == vectorType.S)
				{
					tmp.setWTQ(parentVector.wTQ);
				}*/
				unionVector.add(tmp);
				indexParent++;
			}
			else
			{
				tmp2 = candidateVector.get(indexCandidate);
				/*if(candidateVector.vec_type == vectorType.S)
				{
					tmp2.setWTQ(candidateVector.wTQ);
				}*/
				unionVector.add(tmp2);
				indexCandidate++;
			}
		}
		
		/**
		 *  
		 */
		if(indexParent<parentSize)
		{
			/*if(parentVector.vec_type==vectorType.S)
			{
				parentVector.setWTQ(indexParent);
			}*/
			for(int i=indexParent;i<parentSize;i++)
			{
				
				unionVector.add(parentVector.get(i));
			}
		}
		else if(indexCandidate<candidateSize)
		{
			/*if(candidateVector.vec_type==vectorType.S)
			{
				candidateVector.setWTQ(indexCandidate);
			}*/
			for(int i=indexCandidate;i<candidateSize;i++)
			{
				unionVector.add(candidateVector.get(i));
			}
		}
		
		return unionVector;
	}

	private static DocVector intersect(DocVector parentVector, DocVector candidateVector) 
	{
		if(parentVector.isEmpty() || candidateVector.isEmpty())
		{
			System.out.println("Empty List received");
			return null;
		}

		DocVec_Element tmp,tmp2;
		DocVector intersectionVector = new DocVector();
		
		int indexParent = 0, indexCandidate = 0;
		int parentSize=parentVector.size();
		int candidateSize=candidateVector.size();
		
		while(indexParent < parentSize && indexCandidate < candidateSize)
		{
			// we find that the 2 terms have occured in the same document
			if(parentVector.get(indexParent).equals(candidateVector.get(indexCandidate)))
			{
				// add termstats from 2nd docVector to 1st docVector --
				tmp = parentVector.get(indexParent);
				tmp.add(candidateVector.get(indexCandidate).statsList);
				
				tmp = parentVector.get(indexParent);
				tmp2 = candidateVector.get(indexCandidate);
				/**
				 * store the wright_Term_Query
				 * Set the term queries weights
				 */
				/*if(parentVector.vec_type == vectorType.S)
				{
					tmp.setWTQ(parentVector.wTQ);
				}
				if(candidateVector.vec_type == vectorType.S)
				{
					tmp2.setWTQ(candidateVector.wTQ);
				}*/
				tmp.add(tmp2.statsList);
				intersectionVector.add(tmp);
				indexParent++;
				indexCandidate++;
			}
			else if((parentVector.get(indexParent)).docID <(candidateVector.get(indexCandidate)).docID)
			{
				indexParent++;
			}
			else
			{
				indexCandidate++;
			}
		}
		
		return intersectionVector;
	}

	public static ArrayList<Integer> sort(DocVector ...candidateVectors)
	{
		TreeMap<Integer, ArrayList<Integer>> vectors_sortedbyLength= new TreeMap<Integer, ArrayList<Integer>>();
		int index=0;
		
		// Sorting postingList -- assuming that two postingList don't have same size
		for(DocVector vector:candidateVectors)
		{
			if(vector == null) 
			{
				System.out.println("The term  does not exit");
			}
			if(!vectors_sortedbyLength.containsKey(vector.size()))
			{
				ArrayList<Integer> a = new ArrayList<Integer>(); a.add(index);
				vectors_sortedbyLength.put(vector.size(),a);
			}
			else
			{
				vectors_sortedbyLength.get(vector.size()).add(index);
			}
			index++;
		}
		
		ArrayList<Integer> Order=new ArrayList<Integer>();
		for(int sortID : vectors_sortedbyLength.keySet())
		{
			Order.addAll(vectors_sortedbyLength.get(sortID));
		}
		
		return Order;
	}
	
	
	public static void checkResultVector(DocVector result)
	{
		
		int check =0;
		for(DocVec_Element ele : result.vector)
		{
			if(check > ele.docID)
			{
				System.out.println("WE ARE NOT IN A GOOD PLACE");
				
			}
			check=ele.docID;
			
		}
	}
}
