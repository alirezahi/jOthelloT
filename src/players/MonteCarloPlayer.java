package players;

/**
 *
 * @author Alireza Heydari
 */
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;
import java.util.Date;

import game.*;

public class MonteCarloPlayer extends AbstractPlayer {
    static int X = 1, O = -1, validate;

    public static String winner;
    public static void print(String string) {
        System.out.println(string);
    }

    public MonteCarloPlayer(int depth) {
        super(depth);
    }


    public class Node{
        public List<Move> unexamined;
        public BoardSquare action;
        OthelloGame game;
        public int wins = 0;
        public int visits = 0;
        public Node parent;
        public ArrayList<Node> children = new ArrayList<>();
        public int activePlayer ;
        public Node(Node parentNode,int [][] board, BoardSquare boardSquare,OthelloGame game){
            this.action = boardSquare;
            this.game = game;
            this.wins = 0;
            this.visits = 0;
            this.parent = parentNode;
            this.unexamined = game.getValidMoves(board,getMyBoardMark());
            this.children = new ArrayList<>();
            this.activePlayer =1;
        }
        public Node addChild(int [][] board,int index){
            Node node = new Node(this, board, this.unexamined.get(index).getBardPlace(),game);
            this.unexamined.remove(index);
            this.children.add(node);
            return node;
        }

        public Node selectChild(){
            Node selected = null;
            double bestVal = Double.NEGATIVE_INFINITY;
            for(int i=0;i<this.children.size();i++){
                Node child = this.children.get(i);
                double uctValue = child.wins / child.visits + Math.sqrt(2 * Math.log(this.visits) / child.visits);
                if(uctValue > bestVal){
                    selected = child;
                    bestVal = uctValue;
                }
            }
            return selected;
        }

        public Node mostVisitedNode(){
            Node mostVisited = this.children.get(0);
            for(int i=1;i<this.children.size();i++){
                if(this.children.get(i).visits > mostVisited.visits){
                    mostVisited = this.children.get(i);
                }
            }
            return mostVisited;
        }

        public void update(int[] result){
            ++this.visits;
            this.wins += result[this.activePlayer];
        }
    }
    @Override
    public BoardSquare play(int[][] tab) {
        OthelloGame game = new OthelloGame();
        int maxTime=100000;
        int maxIterations=10000;
        Node root = new Node(null, tab, null,game);
        long startTime = (new Date()).getTime();
        long timeLimit = startTime + maxTime;
        int blockSize = 50;
        int nodesVisited = 0;

        for(int iterations=0; iterations<maxIterations && (new Date()).getTime()<timeLimit; iterations+=blockSize) {
            System.out.println(iterations);
            for (int i = 0; i < blockSize; ++i) {
                System.out.println("ali");
                Node node = root;
                int[][] new_board = tab.clone();

                /* Selection */
                while(node.unexamined.size() == 0 && node.children.size()>0){
                    node = node.selectChild();
                    game.do_move(new_board,node.action,this);
                }
                System.out.println(node.unexamined.size());
                /* Expansion */
                if(node.unexamined.size() > 0){
                    int j = (int)Math.floor(Math.random() * node.unexamined.size());
                    System.out.println("k");
                    game.do_move(new_board,node.unexamined.get(j).getBardPlace(),this);
                    node = node.addChild(new_board,j);
                }

                /* Simulation */
                List<Move> actions =  game.getValidMoves(new_board,getMyBoardMark());
                while(actions.size() > 0){
                    game.do_move(new_board,actions.get((int)(Math.floor(Math.random() * actions.size()))).getBardPlace(),this);
                    ++nodesVisited;
                    actions = game.getValidMoves(new_board,getMyBoardMark());
                }

                /* Backpropagation */
                int[] result = new int[2]; // = game.getResult();
                while (node != null){
                    node.update(result);
                    node = node.parent;
                }
            }
        }

        long duration = (new Date().getTime()) - startTime;

        return root.mostVisitedNode().action;
//
//        OthelloGame jogo = new OthelloGame();
//        Random r = new Random();
//        List<Move> jogadas = jogo.getValidMoves(tab, getMyBoardMark());
//        if (jogadas.size() > 0) {
//            return jogadas.get(r.nextInt(jogadas.size())).getBardPlace();
//        } else {
//            return new BoardSquare(-1, -1);
//        }
    }
    public int random_play(OthelloBoard othelloBoard){
        OthelloGame game = new OthelloGame();
        //Define player 1 class
        AbstractPlayer player = new players.RandomPlayer(2);
        player.setBoardMark(getMyBoardMark());
        player.setOpponentBoardMark(getOpponentBoardMark());
        player.setGame(game);
        //Define player 2 class
        AbstractPlayer player2 = new players.RandomPlayer(2);
        player2.setBoardMark(getOpponentBoardMark());
        player2.setOpponentBoardMark(getMyBoardMark());
        player2.setGame(game);
        BoardSquare boardPlace = null;
        List<Move> moveList;
//        OthelloBoard othelloBoard = new OthelloBoard(game);
        while (true) {
            int result = 0;
//           player 1 plays
            moveList = game.getValidMoves(othelloBoard.getBoard(), player.getMyBoardMark());
            System.out.println('5');
            if (moveList.size() > 0) {
                try {
                    boardPlace = player.play(othelloBoard.getBoard());
                    validate = game.validate_moviment(othelloBoard.getBoard(), boardPlace, player);
                } catch (Exception e) {
                    validate = -2;
                    winner = player2.getClass().toString();
                }
            } else {
                boardPlace = new BoardSquare(-1, -1);
                validate = game.validate_moviment(othelloBoard.getBoard(), boardPlace, player);
            }
            if (validate == 0) {
                othelloBoard.setBoard(game.do_move(othelloBoard.getBoard(), boardPlace, player));
            } else if (validate == -1 || validate == -2) {
                System.err.println(player.getClass().toString() + " WO: invalid play");
                print(player.getClass().toString() + " player1 WO: invalid play");
                break;
            }

            print("Player 1");
            othelloBoard.print();

//            check if player 1 won
            result = game.testing_end_game(othelloBoard.getBoard(), player.getBoardMark());
            if (result != 0) {
                if (result == 1) {
                    winner = player.getClass().toString();
                    print("Player 1 Won");
                } else {
                    winner = "draw";
                    print("Draw");
                }
                break;
            }

//           player 2
            moveList = game.getValidMoves(othelloBoard.getBoard(), player2.getMyBoardMark());
            if (moveList.size() > 0) {
                try {
                    boardPlace = player2.play(othelloBoard.getBoard());
                    validate = game.validate_moviment(othelloBoard.getBoard(), boardPlace, player2);
                } catch (Exception e) {
                    validate = -2;
                    winner = player.getClass().toString();
                }
            } else {
                boardPlace = new BoardSquare(-1, -1);
                validate = game.validate_moviment(othelloBoard.getBoard(), boardPlace, player2);
            }
            if (validate == 0) {
                othelloBoard.setBoard(game.do_move(othelloBoard.getBoard(), boardPlace, player2));
            } else if (validate == -1 || validate == -2) {
                print("player2 WO: invalid play");
                System.err.println(player2.getClass().toString() + " WO: invalid play");
                break;
            }
            print("Player2");
            othelloBoard.print();

//            check if player 2 won
            result = game.testing_end_game(othelloBoard.getBoard(), player2.getBoardMark());
            if (result != 0) {
                if (result == 1) {
                    winner = player2.getClass().toString();
                    print("Player 2 won");
                } else {
                    winner = "draw";
                    print("Draw");
                }
                break;
            }

        }
        return 0;
    }

}
