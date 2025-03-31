package battleship;

import java.util.*;

public class Main {

    private static class Coordinate {
        int row;
        int col;

        Coordinate(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return row == that.row && col == that.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    private static class PlayerData {
        char[][] realGrid;
        List<Set<Coordinate>> ships;

        PlayerData() {
            realGrid = initializeGrid();
            ships = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Player 1, place your ships on the game field");
        PlayerData player1 = setupPlayer("Player 1", scanner);
        promptPass(scanner);

        System.out.println("Player 2, place your ships to the game field");
        PlayerData player2 = setupPlayer("Player 2", scanner);
        promptPass(scanner);

        char[][] fogGrid1 = initializeGrid();
        char[][] fogGrid2 = initializeGrid();

        boolean isPlayer1Turn = true;
        boolean gameOver = false;

        while (!gameOver) {
            if (isPlayer1Turn) {
                gameOver = playTurn("Player 1", fogGrid1, player1.realGrid, player2.realGrid, player2.ships, scanner);
                if (player2.ships.isEmpty()) {
                    System.out.println("Player 1 sank the last ship. You won. Congratulations!");
                    gameOver = true;
                }
            } else {
                gameOver = playTurn("Player 2", fogGrid2, player2.realGrid, player1.realGrid, player1.ships, scanner);
                if (player1.ships.isEmpty()) {
                    System.out.println("Player 2 sank the last ship. You won. Congratulations!");
                    gameOver = true;
                }
            }
            if (!gameOver) {
                promptPass(scanner);
                isPlayer1Turn = !isPlayer1Turn;
            }
        }

        scanner.close();
    }

    private static PlayerData setupPlayer(String playerName, Scanner scanner) {
        PlayerData data = new PlayerData();

        printGrid(data.realGrid);

        String[] shipNames = {"Aircraft Carrier", "Battleship", "Submarine", "Cruiser", "Destroyer"};
        int[] shipSizes = {5, 4, 3, 3, 2};

        for (int i = 0; i < shipNames.length; i++) {
            boolean placed = false;
            while (!placed) {
                System.out.println("Enter the coordinates of the " + shipNames[i] + " (" + shipSizes[i] + " cells):");
                String input = scanner.nextLine().trim();
                String[] parts = input.split("\\s+");
                if (parts.length != 2) {
                    System.out.println("Error! Invalid number of coordinates. Try again:");
                    continue;
                }

                String startCoord = parts[0];
                String endCoord = parts[1];

                int[] start = new int[2];
                int[] end = new int[2];
                if (!parseCoordinate(startCoord, start) || !parseCoordinate(endCoord, end)) {
                    System.out.println("Error!");
                    continue;
                }

                int startRow = start[0];
                int startCol = start[1];
                int endRow = end[0];
                int endCol = end[1];

                if (startRow < 0 || startRow >= 10 || startCol < 0 || startCol >= 10 ||
                        endRow < 0 || endRow >= 10 || endCol < 0 || endCol >= 10) {
                    System.out.println("Error!");
                    continue;
                }

                if (startRow != endRow && startCol != endCol) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    continue;
                }

                int length;
                if (startRow == endRow) {
                    length = Math.abs(endCol - startCol) + 1;
                } else {
                    length = Math.abs(endRow - startRow) + 1;
                }

                if (length != shipSizes[i]) {
                    System.out.println("Error! Wrong length of the " + shipNames[i] + "! Try again:");
                    continue;
                }

                boolean cellsOccupied = false;
                if (startRow == endRow) {
                    int minCol = Math.min(startCol, endCol);
                    int maxCol = Math.max(startCol, endCol);
                    for (int col = minCol; col <= maxCol; col++) {
                        if (data.realGrid[startRow][col] == 'O') {
                            cellsOccupied = true;
                            break;
                        }
                    }
                } else {
                    int minRow = Math.min(startRow, endRow);
                    int maxRow = Math.max(startRow, endRow);
                    for (int row = minRow; row <= maxRow; row++) {
                        if (data.realGrid[row][startCol] == 'O') {
                            cellsOccupied = true;
                            break;
                        }
                    }
                }

                if (cellsOccupied || isAdjacent(data.realGrid, startRow, startCol, endRow, endCol)) {
                    System.out.println("Error! You placed it too close to another one. Try again:");
                    continue;
                }

                placeShip(data.realGrid, startRow, startCol, endRow, endCol);

                Set<Coordinate> shipCoordinates = new HashSet<>();
                if (startRow == endRow) {
                    int minCol = Math.min(startCol, endCol);
                    int maxCol = Math.max(startCol, endCol);
                    for (int col = minCol; col <= maxCol; col++) {
                        shipCoordinates.add(new Coordinate(startRow, col));
                    }
                } else {
                    int minRow = Math.min(startRow, endRow);
                    int maxRow = Math.max(startRow, endRow);
                    for (int row = minRow; row <= maxRow; row++) {
                        shipCoordinates.add(new Coordinate(row, startCol));
                    }
                }
                data.ships.add(shipCoordinates);

                printGrid(data.realGrid);
                placed = true;
            }
        }

        return data;
    }

    private static boolean playTurn(String playerName, char[][] fogGrid, char[][] currentRealGrid, char[][] opponentRealGrid, List<Set<Coordinate>> opponentShips, Scanner scanner) {
        System.out.println(playerName + ", it's your turn:");
        printGrid(fogGrid);
        System.out.println("---------------------");
        printGrid(currentRealGrid);

        int row = -1;
        int col = -1;
        boolean validShot = false;

        while (!validShot) {
            System.out.println("\nTake a shot!");
            String input = scanner.nextLine().trim();
            int[] coord = new int[2];
            if (!parseCoordinate(input, coord)) {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
                continue;
            }

            row = coord[0];
            col = coord[1];

            if (row < 0 || row >= 10 || col < 0 || col >= 10) {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
                continue;
            }

            validShot = true;
        }

        boolean hit = false;
        if (opponentRealGrid[row][col] == 'O') {
            hit = true;
            opponentRealGrid[row][col] = 'X';
            fogGrid[row][col] = 'X';
        } else if (opponentRealGrid[row][col] == '~') {
            fogGrid[row][col] = 'M';
        }

        boolean sankShip = false;
        if (hit) {
            Coordinate hitCoord = new Coordinate(row, col);
            Set<Coordinate> hitShip = null;
            for (Set<Coordinate> ship : opponentShips) {
                if (ship.contains(hitCoord)) {
                    hitShip = ship;
                    break;
                }
            }

            if (hitShip != null) {
                boolean allHit = true;
                for (Coordinate c : hitShip) {
                    if (opponentRealGrid[c.row][c.col] != 'X') {
                        allHit = false;
                        break;
                    }
                }
                if (allHit) {
                    opponentShips.remove(hitShip);
                    sankShip = true;
                }
            }
        }

        System.out.println();
        printGrid(fogGrid);
        System.out.println("---------------------");
        printGrid(currentRealGrid);

        if (hit) {
            if (sankShip) {
                if (opponentShips.isEmpty()) {
                    return true;
                } else {
                    System.out.println("You sank a ship! Specify a new target:");
                }
            } else {
                System.out.println("You hit a ship!");
            }
        } else {
            System.out.println("You missed!");
        }

        return false;
    }

    private static char[][] initializeGrid() {
        char[][] grid = new char[10][10];
        for (int i = 0; i < 10; i++) {
            Arrays.fill(grid[i], '~');
        }
        return grid;
    }

    private static void printGrid(char[][] grid) {
        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        for (int i = 0; i < 10; i++) {
            char rowLabel = (char) ('A' + i);
            StringBuilder rowLine = new StringBuilder();
            rowLine.append(rowLabel).append(" ");
            for (int j = 0; j < 10; j++) {
                rowLine.append(grid[i][j]);
                if (j < 9) {
                    rowLine.append(" ");
                }
            }
            System.out.println(rowLine);
        }
    }

    private static boolean parseCoordinate(String coord, int[] rowCol) {
        if (coord == null || coord.isEmpty() || coord.length() < 2) {
            return false;
        }

        char rowChar = Character.toUpperCase(coord.charAt(0));
        if (rowChar < 'A' || rowChar > 'J') {
            return false;
        }
        int row = rowChar - 'A';

        String numPart = coord.substring(1);
        try {
            int col = Integer.parseInt(numPart);
            if (col < 1 || col > 10) {
                return false;
            }
            rowCol[0] = row;
            rowCol[1] = col - 1;
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isAdjacent(char[][] grid, int startRow, int startCol, int endRow, int endCol) {
        int minRow = Math.min(startRow, endRow);
        int maxRow = Math.max(startRow, endRow);
        int minCol = Math.min(startCol, endCol);
        int maxCol = Math.max(startCol, endCol);

        for (int i = minRow - 1; i <= maxRow + 1; i++) {
            for (int j = minCol - 1; j <= maxCol + 1; j++) {
                if (i >= 0 && i < 10 && j >= 0 && j < 10) {
                    if (grid[i][j] == 'O') {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void placeShip(char[][] grid, int startRow, int startCol, int endRow, int endCol) {
        if (startRow == endRow) {
            int minCol = Math.min(startCol, endCol);
            int maxCol = Math.max(startCol, endCol);
            for (int j = minCol; j <= maxCol; j++) {
                grid[startRow][j] = 'O';
            }
        } else {
            int minRow = Math.min(startRow, endRow);
            int maxRow = Math.max(startRow, endRow);
            for (int i = minRow; i <= maxRow; i++) {
                grid[i][startCol] = 'O';
            }
        }
    }

    private static void promptPass(Scanner scanner) {
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}