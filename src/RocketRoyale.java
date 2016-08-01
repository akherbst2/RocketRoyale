import java.util.*;
import java.util.ArrayDeque;

/**
 * Max flow problem.  Solved as part of a timed programming competition, comments and formatting
 * added afterwards.
 * Link: https://pcs.spruett.me/problems/40
 *
 * @author Alyssa Herbst
 * 6/5/16
 */
public class RocketRoyale {
    private static ArrayList<Node> nodes, teams, series;
    private static HashMap<String, Integer> start,indices;
    private static Node S, K;
    private static int N, C, F;
    private static String init;

    public static void main(String[] args) {
        //reads in values
        readInit();
        ArrayList<Node> output = new ArrayList<>();
        setup(init);

        //We test each team to see if they could be a possible winner of the royale.  Which teams
        //can be ruled out entirely?
        for (int i = 0; i < N; i++) {
            Node fave = teams.get(i);

            //Calculates the total number of series that a team has yet to play.  These games are
            //"up for grabs".
            int sumSeries = 0;
            for (Node ser : series) {
                for (Edge edge : ser.edges) {
                    if (edge.to.equals(fave)) {
                        sumSeries++;
                    }
                }
            }

            //Calculates the maximum number of games that a team could win.  Assuming all goes well,
            //could this team win the royale?
            int maxWins = start.get(fave.name) + sumSeries * 5;
            for (Node team : teams) {
                if (team.equals(fave)) {

                    //We allow the test team to win as many games as needed to win the royale.
                    link(team, K, Integer.MAX_VALUE);
                }
                else {

                    //We allow other teams to only win as many games to tie with the test team.
                    link(team, K, maxWins - start.get(team.name));
                }
            }

            //We try to "push" as many games as we can through the system.
            int maxFlow = getMaxFlow();

            //If all games could be played, with the test team as the winner, then add the team to
            //the list of possible output.
            if (maxFlow == series.size() * 5) {
                output.add(fave);
            }
            setup(init);
        }

        //Sort the output.  Make it look pretty.
        output.sort((o1, o2) -> o1.name.compareTo(o2.name));

        for (Node n : output) {
            System.out.println(n.name);
        }
    }

    /**
     * Finds the maximum flow that may be pushed through a weighted graph.
     *
     * @return Returns the maximum flow.
     */
    private static int getMaxFlow() {
        int total = 0;

        for (; ; ) {
            Edge[] prev   = new Edge[nodes.size()];
            int addedFlow = findAugmentingPath(S, K, prev);

            //Stop when we can no longer add flow.
            if (addedFlow == 0) {
                break;
            }
            total += addedFlow;

            //Once we've found a path, update the flow through the previous edges.
            for (Edge edge = prev[K.i];
                 edge != null;
                 edge = prev[edge.dual.to.i]) {
                edge.addFlow(addedFlow);
            }
        }
        return total;
    }

    /**
     * Reads the initial values through the scanner.
     */
    private static void readInit() {
        Scanner       sc  = new Scanner(System.in);
        StringBuilder bob = new StringBuilder();

        N = sc.nextInt();
        C = sc.nextInt();
        F = sc.nextInt();

        for (int k = 0; k < (N + C * 4 + F * 2); k++) {
            bob.append(sc.next());
            bob.append(" ");
        }
        init = bob.toString();
    }

    /**
     * Sets up the Maximum Flow System.
     *
     * @param in The input in String format.
     */
    private static void setup(String in) {
        Scanner sc = new Scanner(in);
        indices    = new HashMap<>();
        teams      = new ArrayList<>();
        series     = new ArrayList<>();
        nodes      = new ArrayList<>();
        S          = addNode("source");
        K          = addNode("sink");
        start      = new HashMap<>();

        for (int k = 0; k < N; k++) {
            String name = sc.next();
            Node   team = addNode(name);
            teams.add(team);
            start.put(name, 0);
        }
        for (int k = 0; k < C; k++) {
            String t1  = sc.next();
            String t2  = sc.next();
            Integer s1 = Integer.parseInt(sc.next());
            Integer s2 = Integer.parseInt(sc.next());
            if (start.containsKey(t1)) {
                start.put(t1, start.get(t1) + s1);
            }
            else {
                start.put(t1, s1);
            }
            if (start.containsKey(t2)) {
                start.put(t2, start.get(t2) + s2);
            }
            else {
                start.put(t2, s2);
            }
        }

        for (int k = 0; k < F; k++) {
            String t1 = sc.next();
            String t2 = sc.next();
            Node ser  = addNode("series" + k);
            series.add(ser);
            link(S, ser, 5);
            link(ser, getNode(t1), 5);
            link(ser, getNode(t2), 5);

        }
    }

    /**
     * Creates a link between two nodes with a certain capacity.
     *
     * @param n1       The first node.
     * @param n2       The second node
     * @param capacity Capacity of flow between the two nodes.
     */
    private static void link(Node n1, Node n2, int capacity) {
        Edge e12 = new Edge(n1, n2, capacity);
        Edge e21 = new Edge(n2, n1, 0);
        n1.edges.add(e12);
        e12.dual = e21;
        e21.dual = e12;
        n2.edges.add(e21);
    }

    /**
     * Finds a path through which flow can be pushed.
     *
     * @param src  The node from which the source of the flow originates
     * @param snk  The node which receives flow
     * @param from Edge array
     * @return     Returns the amount of flow that can be pushed
     */
    private static int findAugmentingPath(Node src, Node snk, Edge[] from) {
        Deque<Node> queue = new ArrayDeque<>();
        queue.offer(src);

        int N = nodes.size();
        int[] minCapacity = new int[N];
        boolean[] visited = new boolean[N];
        visited[src.i] = true;
        Arrays.fill(minCapacity, Integer.MAX_VALUE);

        while (queue.size() > 0) {
            Node node = queue.poll();
            if (node == snk) {
                return minCapacity[snk.i];
            }
            for (Edge edge : node.edges) {
                Node dest = edge.to;

                //If it has remaining flow to push through and it's not visited
                if (edge.remaining() > 0 && !visited[dest.i]) {
                    visited[dest.i]     = true;
                    from[dest.i]        = edge;
                    minCapacity[dest.i] = Math.min(minCapacity[node.i], edge.remaining());

                    //can bail as soon as we see sink.
                    if (dest == snk) {
                        return minCapacity[snk.i];
                    }
                    else {
                        queue.push(dest);
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Finds a node by name
     *
     * @param name name of the node
     * @return  Returns a Node
     */
    private static Node getNode(String name) {
        int idx = indices.get(name);
        return nodes.get(idx);
    }

    /**
     * Adds a node to a Hashmap of Nodes by indices
     *
     * @param name  Name of a new Node
     * @return      The Node that was created
     */
    private static Node addNode(String name) {
        int idx = nodes.size();
        Node n  = new Node(name, idx);
        nodes.add(n);
        indices.put(name, idx);
        return n;
    }

    /**
     * Connections between two nodes.
     */
    private static class Edge {
        Node from, to;
        int capacity, flow;
        Edge dual;

        /**
         * Constructor
         *
         * @param from  First Node
         * @param to    Second Node
         * @param cap   Capacity between these two Nodes
         */
        Edge(Node from, Node to, int cap) {
            this.to       = to;
            this.flow     = 0;
            this.from     = from;
            this.capacity = cap;
        }

        /**
         * Updates the flow.  Also updates the Edge's corresponding dual node.
         *
         * @param amount    Amount of flow to be pushed through the Edge.
         */
        void addFlow(int amount) {
            flow += amount;
            if (dual != null) {
                dual.flow -= amount;
            }
        }

        /**
         * Returns the remaining flow that can be pushed through the Edge.
         *
         * @return  remaining flow
         */
        int remaining() {
            return capacity - flow;
        }
    }

    /**
     * Nodes in a graph
     */
    private static class Node {
        ArrayList<Edge> edges;
        String name;
        int i;

        /**
         * Constructor
         *
         * @param name  Name of the Node
         * @param i     Index of the Node
         */
        Node(String name, int i) {
            this.name = name;
            this.i = i;
            this.edges = new ArrayList<>();
        }

        /**
         * Returns the hashcode for a node.  Allows for hashing.
         *
         * @return  Hashcode
         */
        @Override
        public int hashCode() {
            return i;
        }

        /**
         * Tests equality of Nodes by looking at the index.
         *
         * @param o Other object to be examined
         * @return  True if other Object is a Node and has the same Index.
         */
        @Override
        public boolean equals(Object o) {
            if (o.getClass() == Node.class) {
                Node n = (Node) o;
                return n.i == i;
            }
            return false;
        }
    }
}