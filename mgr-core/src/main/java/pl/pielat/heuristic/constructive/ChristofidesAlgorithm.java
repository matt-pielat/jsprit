package pl.pielat.heuristic.constructive;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Place;

import java.util.*;

public class ChristofidesAlgorithm
{
    private class Edge
    {
        int indexA;
        int indexB;
    }

    private class Graph
    {
        private ArrayList<LinkedList<Integer>> edges;

        public Graph(int vertexCount)
        {
            //noinspection unchecked
            edges = new ArrayList<>(vertexCount);
        }

        public void addEdges(Collection<Edge> newEdges)
        {
            for (Edge newEdge : newEdges)
            {
                edges.get(newEdge.indexA).add(newEdge.indexB);
                edges.get(newEdge.indexB).add(newEdge.indexA);
            }
        }

        public boolean hasEdges(int vertex)
        {
            return !edges.get(vertex).isEmpty();
        }

        public Integer removeLastEdge(Integer vertex)
        {
            LinkedList<Integer> adjacentVertices = edges.get(vertex);
            Integer adjacentVertex = adjacentVertices.removeLast();
            edges.get(adjacentVertex).removeLastOccurrence(vertex);
            return adjacentVertex;
        }
    }

    private ProblemInfo.TransportCostFunction costFunction;
    private boolean costAsymmetry; //TODO assign

    public ChristofidesAlgorithm(ProblemInfo.TransportCostFunction costFunction)
    {
        this.costFunction = costFunction;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public ArrayList<Integer> solveTsp(ArrayList<Place> nodes)
    {
        ArrayList<Edge> minimumSpanningTree = findMinimumSpanningTree(nodes);

        int[] vertexDegrees = new int[nodes.size()];
        for (Edge edge : minimumSpanningTree)
        {
            vertexDegrees[edge.indexA]++;
            vertexDegrees[edge.indexB]++;
        }

        ArrayList<Place> oddDegreeVertices = new ArrayList<>(nodes.size());
        for (int i = 0; i < vertexDegrees.length; i++)
        {
            if (vertexDegrees[i] % 2 == 0)
                continue;
            oddDegreeVertices.add(nodes.get(i));
        }

        Graph multigraph = new Graph(nodes.size());
        multigraph.addEdges(minimumSpanningTree);
        multigraph.addEdges(findPerfectMatching(oddDegreeVertices));

        return findEulerTour(multigraph, nodes.size());
    }

    private ArrayList<Edge> findMinimumSpanningTree(ArrayList<Place> nodes)
    {
        ArrayList<Edge> minimumSpanningTree = new ArrayList<>(nodes.size() - 1);

        LinkedList<Integer> unvisitedNodes = new LinkedList<>();
        double[] shortestCosts = new double[nodes.size()];
        int[] shortestCostEdgeEndpoints = new int[nodes.size()];

        for (int i = 0; i < nodes.size(); i++)
        {
            unvisitedNodes.add(i);
            shortestCosts[i] = Double.POSITIVE_INFINITY;
        }

        int lastVisitedNodeIndex = unvisitedNodes.removeFirst();

        while (!unvisitedNodes.isEmpty())
        {
            Place from = nodes.get(lastVisitedNodeIndex);
            int closestNodeIndex = 0;

            for (int nodeIndex : unvisitedNodes)
            {
                Place to = nodes.get(nodeIndex);
                double distance = costFunction.getCost(from, to);

                if (distance < shortestCosts[nodeIndex])
                {
                    shortestCosts[nodeIndex] = distance;
                    shortestCostEdgeEndpoints[nodeIndex] = lastVisitedNodeIndex;
                }

                if (shortestCosts[nodeIndex] < shortestCosts[closestNodeIndex])
                    closestNodeIndex = nodeIndex;
            }

            Edge edge = new Edge();
            edge.indexA = shortestCostEdgeEndpoints[closestNodeIndex];
            edge.indexB = closestNodeIndex;
            minimumSpanningTree.add(edge);

            unvisitedNodes.removeFirstOccurrence(closestNodeIndex);
            lastVisitedNodeIndex = closestNodeIndex;
        }

        return minimumSpanningTree;
    }

    private ArrayList<Edge> findPerfectMatching(ArrayList<Place> nodes)
    {
        //TODO more elaborate algorithm is needed here

        ArrayList<Edge> matching = new ArrayList<>(nodes.size() / 2);
        boolean[] doneVertices = new boolean[nodes.size()];

        for (int i = 1; i < nodes.size(); i++)
        {
            if (doneVertices[i])
                continue;

            Place from = nodes.get(i);

            int jBest = -1;
            double minCost = Double.POSITIVE_INFINITY;

            for (int j = 0; j < i; j++)
            {
                if (doneVertices[j])
                    continue;

                Place to = nodes.get(j);

                double cost = costFunction.getCost(from, to);
                if (minCost > cost)
                {
                    minCost = cost;
                    jBest = j;
                }
            }

            doneVertices[i] = true;
            doneVertices[jBest] = true;

            Edge edge = new Edge();
            edge.indexA = i;
            edge.indexB = jBest;
            matching.add(edge);
        }

        return matching;
    }

    private ArrayList<Integer> findEulerTour(Graph multigraph, int vertexCount)
    {
        boolean[] visitedVertices = new boolean[vertexCount];

        Stack<Integer> currentPath = new Stack<>();
        ArrayList<Integer> circuit = new ArrayList<>(vertexCount);

        currentPath.push(0);
        int currentVertex = 0;

        while (!currentPath.empty())
        {
            if (multigraph.hasEdges(currentVertex))
            {
                currentPath.push(currentVertex);
                currentVertex = multigraph.removeLastEdge(currentVertex);
            }
            else
            {
                if (!visitedVertices[currentVertex])
                {
                    circuit.add(currentVertex);
                    visitedVertices[currentVertex] = true;
                }
                currentVertex = currentPath.pop();
            }
        }

        return circuit;
    }
}
