package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <a href="https://www.codewars.com/kata/5588bd9f28dbb06f43000085/train/java">This is a Kata from code wars site</a>
 *
 * <p>
 * There are several difficulty of sudoku games, we can estimate the difficulty of a sudoku game based on how many
 * cells are given of the 81 cells of the game.
 * </p>
 * <ul>
 * <li>Easy sudoku generally have over 32 givens</li>
 *
 * <li>Medium sudoku have around 30–32 givens</li>
 *
 * <li>Hard sudoku have around 28–30 givens</li>
 *
 * <li>Very Hard sudoku have less than 28 givens</li>
 *
 * </ul>
 * <b>Note:</b> The minimum of givens required to create a unique (with no multiple solutions) sudoku game is 17.
 * <p>
 * A hard sudoku game means that at start no cell will have a single candidates and thus require guessing
 * and trial and error. A very hard will have several layers of multiple candidates for any empty cell.
 * </p>
 * <p>
 * <b>Task:</b>
 * </p>
 * <p>
 * Write a function that solves sudoku puzzles of any difficulty. The function will take a sudoku grid and it should
 * return a 9x9 array with the proper answer for the puzzle.
 * </p>
 * <p>
 * Or it should raise an error in cases of: invalid grid (not 9x9, cell with values not in the range 1~9);
 * multiple solutions for the same puzzle or the puzzle is unsolvable
 * </p>
 * <p>
 * Java users: throw an IllegalArgumentException for unsolvable or invalid puzzles or when a puzzle has
 * multiple solutions.
 * </p>
 * <p>
 * <b>NOTE:</b> We should try to use some more advanced techniques to solve any difficulty sudoku puzzle.
 * As mentioned by Chat-GPT these techniques can be:
 * <br>
 * <i>Apply various logical deductions such as "naked singles," "hidden singles," "naked pairs," "hidden pairs,"
 * "naked triples," and "hidden triples" to fill in more cells. These deductions involve analyzing the possible
 * values for cells in a row, column, or box to identify patterns and eliminate possibilities.</i>
 * </p>
 * <a href="https://github.com/Gretgor/HardSudokuSolver/blob/main/SudokuProblem.py">This is a sample of a Hard Sudoku
 * Solver using back track strategy.</a>
 */
public class SudokuSolver {

    private final int[][] puzzle;

    private int zerosInPuzzle = 0;

    private final int[][][] puzzlePossibilities;

    private boolean easyAlreadyTried = false;
    private boolean mediumAlreadyTried = false;
    private boolean hardAlreadyTried = false;
    private boolean veryHardAlreadyTried = false;

    private enum GivensInPuzzle {
        VERY_HARD(28),
        HARD(30),
        MEDIUM(32),
        EASY(32);

        private final int value;

        GivensInPuzzle(int value) {
            this.value = value;
        }

        int getValue() {
            return this.value;
        }

        int zerosOnPuzzle() {
            return 81 - value;
        }
    }

    private static final int MAX_UNKNOWN_POSITIONS_IN_PUZZLE = 64;

    private final int[] allPossibilities = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    public SudokuSolver(int[][] grid) throws IllegalArgumentException {
        try {
            this.puzzle = grid;
            puzzlePossibilities = new int[grid.length][grid.length][grid.length];
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid grid puzzle. The grid must be an int[][] type.\nMessage: " +
                    exception.getMessage());
        }
    }

    private void countZerosInPuzzle() {
        for (int[] row : puzzle) {
            for (int element : row) {
                if (element == 0) {
                    zerosInPuzzle += 1;
                }
            }
        }
    }

    /**
     * This method checks when the puzzle board has more than 9 rows or if any of its rows has
     * more than 9 elements on it and, at last it verifies if there is any invalid element on puzzle (e.g. an element
     * that is not a digit between 1 and 9).
     *
     * @throws IllegalArgumentException when any aforementioned conditions are found
     */
    private void startValidatingPuzzle() {
        if (puzzle.length != 9) throw new IllegalArgumentException("It's not a 9x9 grid.");
        if (zerosInPuzzle > MAX_UNKNOWN_POSITIONS_IN_PUZZLE) throw new IllegalArgumentException("Invalid puzzle. The minimum of " +
                "givens required to create an unique (with no multiple solutions) sudoku game is 17.");
        for (int rowIndex = 0; rowIndex < puzzle.length; rowIndex += 1) {
            if (puzzle[rowIndex].length != 9) throw new IllegalArgumentException("The row at " + rowIndex +
                    " index has an invalid length.");
            for (int element : puzzle[rowIndex]) {
                if (element != 0 && (element < 1 || element > 9)) throw new IllegalArgumentException("At least one " +
                        "element is not a valid digit. Only digits from 1 to 9 are allowed.");
            }
        }
    }

    /**
     * This method builds an array with all current row elements, including the ones that are not set yet.
     * @param rowIndex the row index
     * @return an array with all elements of the row
     */
    public int[] getCurrentRow(int rowIndex) {
        return puzzle[rowIndex];
    }

    /**
     * This method builds an array with all current column elements, including the ones that are not set yet.
     * @param columnIndex the column index
     * @return an array with all elements of the column
     */
    public int[] getCurrentColumn(int columnIndex) {
        return Arrays.stream(puzzle).mapToInt((row) -> row[columnIndex]).toArray();
    }

    /**
     * This method calculates the 0-indexed number of the quadrant the row belongs to.
     * Example:
     * <br>
     * For a normal Sudoku puzzle (9x9), if row index is 7, this method will return 2:
     * Math.floor(7 / Math.sqrt(9)) = 2
     * @param rowIndex the position row index
     * @return the 0-indexed index the row belongs to (0, 1 or 2)
     */
    public int getRowQuadrant(int rowIndex) {
        return (int) Math.floor(rowIndex / Math.sqrt(puzzle.length));
    }

    /**
     * This method calculates the 0-indexed number of the quadrant the column belongs to.
     * Example:
     * <br>
     * For a normal Sudoku puzzle (9x9), if row index is 3, this method will return 1:
     * Math.floor(3 / Math.sqrt(9)) = 1
     * @param rowIndex the position row index
     * @param columnIndex the position column index
     * @return the 0-indexed index the column belongs to (0, 1 or 2)
     */
    public int getColumnQuadrant(int rowIndex, int columnIndex) {
        return (int) Math.floor(columnIndex / Math.sqrt(puzzle[rowIndex].length));
    }

    /**
     * This method returns the row index of the first (top-left) element from a specific quadrant.
     * @param rowQuadrant the 0-indexed index of the given quadrant
     * @return 0-indexed row index of the most top-left element
     */
    public int getCurrentQuadrantFirstRowIndex(int rowQuadrant) {
        return (int) (rowQuadrant * Math.sqrt(puzzle.length));
    }

    /**
     * This method returns the column of the first (top-left) element form a specific quadrant.
     * @param columnQuadrant the 0-indexed index of the given column quadrant
     * @param rowIndex the 0-indexed element row index (this is for future use, since for a 9x9 puzzle it will never change)
     * @return 0-indexed column index of the most top-left element
     */
    public int getCurrentQuadrantFirstColumnIndex(int columnQuadrant, int rowIndex) {
        return (int) (columnQuadrant * Math.sqrt(puzzle[rowIndex].length));
    }

    /**
     * This method builds a list of integers that contains all elements from a given quadrant.
     * @param currentQuadrantFirstRowPosition 0-indexed row index of the current quadrant
     * @param currentQuadrantFirstColumnPosition 0-indexed column index of the current quadrant
     * @return a list of integer with all elements of the current quadrant (including the ones that are not set yet)
     */
    public List<Integer> getCurrentQuadrantNumbers(int currentQuadrantFirstRowPosition,
                                                   int currentQuadrantFirstColumnPosition) {
        List<Integer> quadrantNumbers = new ArrayList<>();
        for (int i = 0; i < Math.sqrt(puzzle.length); i += 1) {
            for (int j = 0; j < Math.sqrt(puzzle[currentQuadrantFirstRowPosition + i].length); j += 1) {
                quadrantNumbers.add(
                        puzzle[currentQuadrantFirstRowPosition + i][currentQuadrantFirstColumnPosition + j]
                );
            }
        }
        return quadrantNumbers;
    }

    /**
     * An auxiliary method to convert a list of integers into a primitive array of int.
     * @param list the list with integer elements
     * @return an array of int with all the list elements
     */
    private int[] integerListToIntArray(List<Integer> list) {
        try {
            int[] quadrantToArray = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                quadrantToArray[i] = list.get(i);
            }
            return quadrantToArray;
        } catch (Exception e) {
            throw new RuntimeException("Impossible to convert list to array.");
        }
    }

    /**
     * This method builds an array of int that contains all elements from a given quadrant.
     * @param rowIndex 0-indexed row index of the current quadrant
     * @param columnIndex 0-indexed column index of the current quadrant
     * @return an array of int with all elements of the current quadrant (including the ones that are not set yet)
     */
    public int[] getCurrentQuadrant(int rowIndex, int columnIndex) {
        int rowQuadrant = getRowQuadrant(rowIndex);
        int columnQuadrant = getColumnQuadrant(rowIndex, columnIndex);
        int currentQuadrantFirstRowPosition = getCurrentQuadrantFirstRowIndex(rowQuadrant);
        int currentQuadrantFirstColumnPosition = getCurrentQuadrantFirstColumnIndex(columnQuadrant, rowIndex);

        List<Integer> quadrantNumbers = getCurrentQuadrantNumbers(currentQuadrantFirstRowPosition,
                currentQuadrantFirstColumnPosition);

        return integerListToIntArray(quadrantNumbers);
    }

    /**
     * This method calculates the symmetric difference between two arrays, more specifically the
     * array that contains all possible numbers (usually 1 to 9 digits) and the current set of possible
     * numbers that already exists on row, column or quadrant.<br><br>
     * <p>
     * Example: if the current position has a set of { 1, 4, 5, 9} as numbers that are already set
     * on that row, column or quadrant, the symmetric difference will be: { 2, 3, 6, 7, 8}
     *
     * @param set the numbers that are already present on row, column or quadrant for the current position
     * @return an array of integer elements that can possibly fit on that position
     */
    public int[] symmetricDifference(int[] set) {
        return Arrays.stream(allPossibilities).filter(possibleNumber ->
                Arrays.stream(set).noneMatch(element ->
                        element == possibleNumber
                )
        ).toArray();
    }

    /**
     * This method calculates the intersection between all sets (row, column and quadrant) for a specific
     * puzzle position.<br><br>
     *
     * Example: if row numbers are { 1, 2, 3, 4, 6 }, column numbers are { 2, 5, 8 }
     * and quadrant numbers are { 1, 2, 3, 7 } the intersection of these sets is
     * a set containing only the number 2, since 2 is the only number present on 3 sets.
     *
     * @param rowSet      row numbers
     * @param columnSet   column numbers
     * @param quadrantSet quadrant numbers
     * @return an array that contains the intersection between all 3 sets of possible numbers
     */
    public int[] getIntersection(int[] rowSet, int[] columnSet, int[] quadrantSet) {
        int[][] allSets = {rowSet, columnSet, quadrantSet};

        Optional<int[]> optionalValue = Arrays.stream(allSets).reduce((int[] accumulator, int[] currentSet) ->
                Arrays.stream(accumulator).filter(number ->
                        Arrays.stream(currentSet).anyMatch(currentSetNumber -> currentSetNumber == number)
                ).toArray()
        );

        return optionalValue.get();
    }

    /**
     * This method generates all candidates for a specific position and create an array wit all possible numbers
     * for that position. This array is set into a 3x3 matrix that contains all possible numbers for all puzzle's
     * positions.
     *
     * @param position the data related to current position such as rowIndex, columnIndex, row, column and quadrant
     */
    private void generateCandidatesFor(PositionData position) {
        int[] possibilitiesForRow = symmetricDifference(position.row);
        int[] possibilitiesForColumn = symmetricDifference(position.column);
        int[] possibilitiesForQuadrant = symmetricDifference(position.quadrant);

        int[] possibleNumbersForCurrentPosition = getIntersection(
                possibilitiesForRow, possibilitiesForColumn, possibilitiesForQuadrant
        );

        puzzlePossibilities[position.rowIndex][position.columnIndex] = possibleNumbersForCurrentPosition;
    }

    /**
     * This method builds a {@link PositionData PositionData} for the current position.
     * @param rowIndex current row index
     * @param columnIndex current column index
     * @return a {@link PositionData PositionData} that contains all info related to the current position
     */
    private PositionData generatePositionDataFor(int rowIndex, int columnIndex) {
        int[] currentRow = getCurrentRow(rowIndex);
        int[] currentColumn = getCurrentColumn(columnIndex);
        int[] currentQuadrant = getCurrentQuadrant(rowIndex, columnIndex);
        return new PositionData(rowIndex, columnIndex, currentRow, currentColumn, currentQuadrant);
    }

    /**
     * This method checks all possible numbers for a specific position on puzzle.
     *
     * @param rowIndex    current row index
     * @param columnIndex current column index
     */
    public void checkCurrentPosition(int rowIndex, int columnIndex) {
        PositionData positionData = generatePositionDataFor(rowIndex, columnIndex);
        generateCandidatesFor(positionData);
        if (puzzlePossibilities[rowIndex][columnIndex].length == 1
                && Arrays.stream(puzzlePossibilities[rowIndex][columnIndex]).findFirst().isPresent()) {
            zerosInPuzzle -= 1;

            puzzle[rowIndex][columnIndex] =
                    Arrays.stream(puzzlePossibilities[rowIndex][columnIndex]).findFirst().getAsInt();
            puzzlePossibilities[rowIndex][columnIndex] = new int[] { };
        }
//        searchHiddenSinglesForRow(positionData);
//        searchHiddenSingleForColumn(positionData);
//        searchHiddenSingleForQuadrant(positionData);
    }

    /**
     * This is an auxiliary method to check if some position is still an empty cell (it has 0 as its value).
     * @param position {@link PositionData PositionData} with current position info
     * @return {@code true} if the current positions has an empty value (e.g. it has zero as its value) or {@code false} otherwise
     */
    private boolean isPositionEmpty(PositionData position) {
        return puzzle[position.rowIndex][position.columnIndex] == 0;
    }

    private boolean contains(int[] set, int element) {
        if (set.length == 0) return false;
        int index = 0;
        while (index < set.length) {
            if (set[index] == element) return true;
            index += 1;
        }
        return false;
    }

    private int[] truthTable() {
        return new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    }

    private int findUniqueOccurrenceBetweenSets(int[][] setsOfCandidates) {
        int[] truthTable = truthTable();
        for (int[] set : setsOfCandidates) {
            for (int element : set) {
                if (element != 0) truthTable[element - 1] += 1;
            }
        }

        for (int index = 0; index < truthTable.length; index++) {
            if (truthTable[index] == 1) return index + 1;
        }
        return -1;
    }

    private void searchHiddenSinglesForRow(PositionData position) {




//        int[] row = puzzle[position.rowIndex];
//        int[][] rowWithCandidates = puzzlePossibilities[position.rowIndex];
//
//        for (int rowElement : row) {
//            if (rowElement == 0) {
//                for (int possibleCandidate : allPossibilities) {
//
//                }
//            }
//        }
//
//
//        for (int possibleCandidate : allPossibilities) {
//
//            int countMatchesForPossibleCandidate = 0;
//
//            for (int columnIndex = 0; columnIndex < puzzle.length; columnIndex += 1) {
//
//                int[] candidatesForPosition = puzzlePossibilities[position.rowIndex][columnIndex];
//
//                for (int candidate : candidatesForPosition) {
//                    if (candidate == possibleCandidate) {
//                        countMatchesForPossibleCandidate += 1;
//                    }
//                }
//
//                if (countMatchesForPossibleCandidate == 1 && isPositionEmpty(position)) {
//                    zerosInPuzzle -= 1;
//                    puzzle[position.rowIndex][position.columnIndex] = possibleCandidate;
//                }
//            }
//        }
    }

    private void searchHiddenSingleForColumn(PositionData position) {
        for (int possibleCandidate : allPossibilities) {

            int countMatchesForPossibleCandidate = 0;

            for (int rowIndex = 0; rowIndex < puzzle.length; rowIndex += 1) {

                int[] candidatesForPosition = puzzlePossibilities[rowIndex][position.columnIndex];

                for (int candidate : candidatesForPosition) {
                    if (candidate == possibleCandidate) {
                        countMatchesForPossibleCandidate += 1;
                    }
                }

                if (countMatchesForPossibleCandidate == 1 && isPositionEmpty(position)) {
                    zerosInPuzzle -= 1;
                    puzzle[position.rowIndex][position.columnIndex] = possibleCandidate;
                }
            }
        }
    }

    private void searchHiddenSingleForQuadrant(PositionData position) {
        for (int possibleCandidate : allPossibilities) {

            int countMatchesForPossibleCandidate = 0;

            int rowQuadrant = getRowQuadrant(position.rowIndex);
            int columnQuadrant = getColumnQuadrant(position.rowIndex, position.columnIndex);
            int currentQuadrantFirstRowPosition = getCurrentQuadrantFirstRowIndex(rowQuadrant);
            int currentQuadrantFirstColumnPosition = getCurrentQuadrantFirstColumnIndex(columnQuadrant, position.rowIndex);

            for (int i = 0; i < Math.sqrt(puzzle.length); i += 1) {
                for (int j = 0; j < Math.sqrt(puzzle[getCurrentQuadrantFirstRowIndex(rowQuadrant) + i].length); j += 1) {

                    var candidates = puzzlePossibilities[currentQuadrantFirstRowPosition + i][currentQuadrantFirstColumnPosition + j];

                    for (int candidate : candidates) {
                        if (candidate == possibleCandidate) {
                            countMatchesForPossibleCandidate += 1;
                        }
                    }

                    if (countMatchesForPossibleCandidate == 1 && isPositionEmpty(position)) {
                        zerosInPuzzle -= 1;
                        puzzle[position.rowIndex][position.columnIndex] = possibleCandidate;
                    }
                }
            }
        }
    }

    public void searchHiddenSingles(PositionData position) {

    }

    /**
     * This method solves easy sudoku puzzles via brute force strategy.
     */
    public void solveEasyMethod() {
        if (easyAlreadyTried) return;
        for (int rowIndex = 0; rowIndex < puzzle.length; rowIndex += 1) {
            for (int columnIndex = 0; columnIndex < puzzle[rowIndex].length; columnIndex += 1) {
                if (puzzle[rowIndex][columnIndex] == 0) {
                    checkCurrentPosition(rowIndex, columnIndex);
                }
            }
        }
        easyAlreadyTried = true;
        mediumAlreadyTried = false;
        hardAlreadyTried = false;
        veryHardAlreadyTried = false;
    }

    public void solveMediumMethod() {
        if (mediumAlreadyTried) return;
        for (int rowIndex = 0; rowIndex < puzzle.length; rowIndex += 1) {
            for (int columnIndex = 0; columnIndex < puzzle[rowIndex].length; columnIndex += 1) {
                if (puzzle[rowIndex][columnIndex] == 0) {
                    checkCurrentPosition(rowIndex, columnIndex);
                    if (puzzle[rowIndex][columnIndex] == 0) {
                        int[][] currentRowCandidates = puzzlePossibilities[rowIndex];
                        int[][] currentColumnCandidates = new int[puzzle.length][puzzle.length];
                        for (int i = 0; i < puzzle.length; i += 1) {
                            currentColumnCandidates[i] = puzzlePossibilities[i][columnIndex];
                        }
                        int[][] currentQuadrantCandidates = new int[puzzle.length][puzzle.length];
                        int rowQuadrant = getRowQuadrant(rowIndex);
                        int columnQuadrant = getColumnQuadrant(rowIndex, columnIndex);
                        int quadrantFirstRowIndex = getCurrentQuadrantFirstRowIndex(rowQuadrant);
                        int quadrantFirstColumnIndex = getCurrentQuadrantFirstColumnIndex(columnQuadrant, rowIndex);
                        List<int[]> auxQuadrantNumbers = new ArrayList<>();
                        for (int i = 0; i < Math.sqrt(puzzle.length); i += 1) {
                            for (int j = 0; j < Math.sqrt(puzzle.length); j += 1) {
                                var elements = puzzlePossibilities[quadrantFirstRowIndex + i][quadrantFirstColumnIndex + j];
                                auxQuadrantNumbers.add(elements);
                            }
                        }
                        for (int i = 0; i < puzzle.length; i += 1) {
                            currentQuadrantCandidates[i] = auxQuadrantNumbers.get(i);
                        }

                        int uniqueForRow = findUniqueOccurrenceBetweenSets(currentRowCandidates);
                        if (uniqueForRow > 0) {

                            for (int j = 0; j < puzzle.length; j += 1) {
                                if (contains(puzzlePossibilities[rowIndex][j], uniqueForRow)) {
                                    puzzle[rowIndex][j] = uniqueForRow;
                                    puzzlePossibilities[rowIndex][j] = new int[] { uniqueForRow };
                                    zerosInPuzzle -= 1;
                                }
                            }

                        }

                        int uniqueForColumn = findUniqueOccurrenceBetweenSets(currentColumnCandidates);
                        if (uniqueForColumn > 0) {
                            for (int i = 0; i < puzzle.length; i += 1) {
                                if (contains(puzzlePossibilities[i][columnIndex], uniqueForRow)) {
                                    puzzle[i][columnIndex] = uniqueForRow;
                                    puzzlePossibilities[i][columnIndex] = new int[] { uniqueForRow };
                                    zerosInPuzzle -= 1;
                                }
                            }
                        }

                        int uniqueForQuadrant = findUniqueOccurrenceBetweenSets(currentQuadrantCandidates);
                        if (uniqueForQuadrant > 0) {
                            for (int i = 0; i < Math.sqrt(puzzle.length); i += 1) {
                                for (int j = 0; j < Math.sqrt(puzzle.length); j += 1) {
                                    if (contains(puzzlePossibilities[quadrantFirstRowIndex + i][quadrantFirstColumnIndex + j], uniqueForQuadrant)) {
                                        puzzle[quadrantFirstRowIndex + i][quadrantFirstColumnIndex + j] = uniqueForQuadrant;
                                        puzzlePossibilities[quadrantFirstRowIndex + i][quadrantFirstColumnIndex + j] = new int[] { uniqueForQuadrant };
                                        zerosInPuzzle -= 1;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        mediumAlreadyTried = true;
        easyAlreadyTried = false;
        hardAlreadyTried = false;
        veryHardAlreadyTried = false;
    }

    public void solveHardMethod() {

    }

    public void solveVeryHardMethod() {

    }

    private void chooseSolverMethod() {
        if (zerosInPuzzle <= GivensInPuzzle.EASY.zerosOnPuzzle() && !easyAlreadyTried) {
            solveEasyMethod();
        } else if ((zerosInPuzzle < GivensInPuzzle.HARD.zerosOnPuzzle() && zerosInPuzzle >= GivensInPuzzle.MEDIUM.zerosOnPuzzle()) || !mediumAlreadyTried) {
            solveMediumMethod();
        } else if (zerosInPuzzle < GivensInPuzzle.VERY_HARD.zerosOnPuzzle() && zerosInPuzzle >= GivensInPuzzle.HARD.zerosOnPuzzle()) {
            solveHardMethod();
        } else {
            solveVeryHardMethod();
        }
    }

    public int[][] solve() {
        countZerosInPuzzle();
        startValidatingPuzzle();
        solveEasyMethod();
        while (zerosInPuzzle > 0) {
            chooseSolverMethod();
        }
        return puzzle;
    }
}

class PositionCoordinates {
    int x;
    int y;

    PositionCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

class PositionData {
    int rowIndex;
    int columnIndex;
    int[] row;
    int[] column;
    int[] quadrant;

    /**
     * PositionData constructor that receives all the data related to the current position
     * @param rowIndex current position row index
     * @param columnIndex current position column index
     * @param row current row numbers
     * @param column current column numbers
     * @param quadrant current quadrant numbers
     */
    PositionData(int rowIndex, int columnIndex, int[] row, int[] column, int[] quadrant) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.row = row;
        this.column = column;
        this.quadrant = quadrant;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int[] getRow() {
        return row;
    }

    public void setRow(int[] row) {
        this.row = row;
    }

    public int[] getColumn() {
        return column;
    }

    public void setColumn(int[] column) {
        this.column = column;
    }

    public int[] getQuadrant() {
        return quadrant;
    }

    public void setQuadrant(int[] quadrant) {
        this.quadrant = quadrant;
    }
}
