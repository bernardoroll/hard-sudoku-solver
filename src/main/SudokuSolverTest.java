package main;

import static org.junit.Assert.*;

import main.SudokuSolver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class SudokuSolverTest {

    private SudokuSolver sudokuSolver;

    private final int[][] baseSamplePuzzle = {
            {0, 0, 6, 1, 0, 0, 0, 0, 8},
            {0, 8, 0, 0, 9, 0, 0, 3, 0},
            {2, 0, 0, 0, 0, 5, 4, 0, 0},
            {4, 0, 0, 0, 0, 1, 8, 0, 0},
            {0, 3, 0, 0, 7, 0, 0, 4, 0},
            {0, 0, 7, 9, 0, 0, 0, 0, 3},
            {0, 0, 8, 4, 0, 0, 0, 0, 6},
            {0, 2, 0, 0, 5, 0, 0, 8, 0},
            {1, 0, 0, 0, 0, 2, 5, 0, 0}
    };

    private final int[] allPossibilities = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    @Test
    @Ignore("Work still in progress")
    public void sampleHardTest() {
        int[][] solution = {
                {3, 4, 6, 1, 2, 7, 9, 5, 8},
                {7, 8, 5, 6, 9, 4, 1, 3, 2},
                {2, 1, 9, 3, 8, 5, 4, 6, 7},
                {4, 6, 2, 5, 3, 1, 8, 7, 9},
                {9, 3, 1, 2, 7, 8, 6, 4, 5},
                {8, 5, 7, 9, 4, 6, 2, 1, 3},
                {5, 9, 8, 4, 1, 3, 7, 2, 6},
                {6, 2, 4, 7, 5, 9, 3, 8, 1},
                {1, 7, 3, 8, 6, 2, 5, 9, 4}};

        assertArrayEquals(solution, sudokuSolver.solve());
    }

    @Test
    public void sampleEasyTest() {
        int[][] easyPuzzle = {
                {3, 4, 6, 1, 2, 7, 9, 5, 8},
                {7, 8, 5, 6, 9, 4, 1, 3, 2},
                {2, 1, 9, 3, 8, 5, 4, 6, 7},
                {4, 6, 2, 5, 3, 1, 8, 7, 9},
                {9, 3, 1, 2, 7, 8, 6, 4, 5},
                {8, 5, 7, 9, 4, 6, 2, 1, 3},
                {5, 9, 8, 4, 1, 3, 7, 2, 6},
                {6, 2, 4, 7, 5, 9, 3, 8, 1},
                {0, 7, 0, 8, 0, 2, 0, 9, 0}};

        int[][] solution = {
                {3, 4, 6, 1, 2, 7, 9, 5, 8},
                {7, 8, 5, 6, 9, 4, 1, 3, 2},
                {2, 1, 9, 3, 8, 5, 4, 6, 7},
                {4, 6, 2, 5, 3, 1, 8, 7, 9},
                {9, 3, 1, 2, 7, 8, 6, 4, 5},
                {8, 5, 7, 9, 4, 6, 2, 1, 3},
                {5, 9, 8, 4, 1, 3, 7, 2, 6},
                {6, 2, 4, 7, 5, 9, 3, 8, 1},
                {1, 7, 3, 8, 6, 2, 5, 9, 4}};

        assertArrayEquals(solution, new SudokuSolver(easyPuzzle).solve());
    }

    @Before
    public void init() {
        sudokuSolver = new SudokuSolver(baseSamplePuzzle);
    }

    @Test
    public void testGetCurrentRow() {
        int[] expectedRow = {0, 8, 0, 0, 9, 0, 0, 3, 0};
        assertArrayEquals(expectedRow, sudokuSolver.getCurrentRow(1));
    }

    @Test
    public void testGetCurrentColumn() {
        int[] expectedColumn = {0, 3, 0, 0, 4, 0, 0, 8, 0};
        assertArrayEquals(expectedColumn, sudokuSolver.getCurrentColumn(7));
    }

    @Test
    public void testGetRowQuadrant() {
        int expectedRowQuadrant = 1;
        assertEquals(expectedRowQuadrant, sudokuSolver.getRowQuadrant(4));
    }

    @Test
    public void testGetColumnQuadrant() {
        int expectedColumnQuadrant = 1;
        assertEquals(expectedColumnQuadrant, sudokuSolver.getColumnQuadrant(3, 4));
    }

    @Test
    public void testGetCurrentQuadrantFirstRowIndex() {
        int expectedFirstRowIndex = 6;
        assertEquals(expectedFirstRowIndex, sudokuSolver.getCurrentQuadrantFirstRowIndex(2));
    }

    @Test
    public void testGetCurrentQuadrantFirstColumnIndex() {
        int expectedFirstColumnIndex = 9;
        assertEquals(expectedFirstColumnIndex, sudokuSolver.getCurrentQuadrantFirstColumnIndex(3, 8));
    }

    @Test
    public void testGetCurrentSquare() {
        int[] quadrantNumbers = { 4, 0, 0, 0, 5, 0, 0, 0, 2 };
        assertArrayEquals(quadrantNumbers, sudokuSolver.getCurrentQuadrant(6, 3));
    }

    @Test
    public void testGetCurrentSquare2() {
        int[] quadrantNumbers = { 4, 0, 0, 0, 5, 0, 0, 0, 2 };
        assertArrayEquals(quadrantNumbers, sudokuSolver.getCurrentQuadrant(8, 4));
    }

    @Test
    public void testGetCurrentSquare3() {
        int[] quadrantNumbers = { 0, 0, 1, 0, 7, 0, 9 ,0, 0 };
        assertArrayEquals(quadrantNumbers, sudokuSolver.getCurrentQuadrant(4, 4));
    }

    @Test
    public void testSymmetricDifference() {
        int[] alreadySetNumbers = { 1, 4, 5, 9 };
        int[] expectedSymmetricDifference = { 2, 3, 6, 7, 8 };
        assertArrayEquals(expectedSymmetricDifference, sudokuSolver.symmetricDifference(alreadySetNumbers));
    }

    @Test
    public void testIntersection() {
        int[] rowSet = { 1, 2, 3, 4, 6 };
        int[] columnSet = { 2, 5, 8 };
        int[] quadrantSet = { 1, 2, 3, 7 };

        int[] expectedIntersection = { 2 };

        assertArrayEquals(expectedIntersection, sudokuSolver.getIntersection(rowSet, columnSet, quadrantSet));
    }

    @Test
    public void testEasyMethodIsCalledForEasyPuzzle() {
        int[][] mediumPuzzle = {
                {0, 4, 0, 1, 0, 7, 9, 5, 0},
                {0, 8, 0, 6, 0, 0, 1, 0, 0},
                {2, 0, 9, 0, 0, 0, 4, 0, 7},
                {0, 6, 0, 5, 0, 1, 0, 7, 0},
                {0, 0, 1, 0, 7, 0, 0, 0, 0},
                {0, 5, 0, 9, 0, 6, 0, 0, 0},
                {5, 0, 0, 0, 1, 0, 0, 2, 6},
                {6, 0, 0, 0, 5, 0, 3, 0, 1},
                {0, 7, 0, 8, 0, 0, 0, 9, 0}};
        int[][] solution = {
                {3, 4, 6, 1, 2, 7, 9, 5, 8},
                {7, 8, 5, 6, 9, 4, 1, 3, 2},
                {2, 1, 9, 3, 8, 5, 4, 6, 7},
                {4, 6, 2, 5, 3, 1, 8, 7, 9},
                {9, 3, 1, 2, 7, 8, 6, 4, 5},
                {8, 5, 7, 9, 4, 6, 2, 1, 3},
                {5, 9, 8, 4, 1, 3, 7, 2, 6},
                {6, 2, 4, 7, 5, 9, 3, 8, 1},
                {1, 7, 3, 8, 6, 2, 5, 9, 4}};

        assertArrayEquals(solution, new SudokuSolver(mediumPuzzle).solve());

    }

    @Test
    public void testMediumMethodIsCalledForMediumPuzzle() {
        int[][] mediumPuzzle = {
                {0, 4, 0, 1, 0, 7, 9, 5, 0},
                {0, 8, 0, 6, 0, 0, 1, 0, 0},
                {2, 0, 9, 0, 0, 0, 4, 0, 7},
                {0, 6, 0, 5, 0, 1, 0, 7, 0},
                {0, 0, 1, 0, 7, 0, 0, 0, 0},
                {0, 5, 0, 9, 0, 6, 0, 0, 0},
                {5, 0, 0, 0, 1, 0, 0, 0, 6},
                {6, 0, 0, 0, 5, 0, 3, 0, 1},
                {0, 7, 0, 8, 0, 0, 0, 9, 0}};
        int[][] solution = {
                {3, 4, 6, 1, 2, 7, 9, 5, 8},
                {7, 8, 5, 6, 9, 4, 1, 3, 2},
                {2, 1, 9, 3, 8, 5, 4, 6, 7},
                {4, 6, 2, 5, 3, 1, 8, 7, 9},
                {9, 3, 1, 2, 7, 8, 6, 4, 5},
                {8, 5, 7, 9, 4, 6, 2, 1, 3},
                {5, 9, 8, 4, 1, 3, 7, 2, 6},
                {6, 2, 4, 7, 5, 9, 3, 8, 1},
                {1, 7, 3, 8, 6, 2, 5, 9, 4}};

        assertArrayEquals(solution, new SudokuSolver(mediumPuzzle).solve());
    }


    int[][][] puzzlePossibilitiesSample = {
            {
                { 2, 3, 6 }, { 4 }, { 3, 5 }, { 1 }, { 2, 3, 8 }, { 7 }, { 9 }, { 5 }, { 2, 3, 6, 8 }
            },
            {
                { 2, 4 }, { 8 }, { 3, 6 }, { 6 }, { 0 }, { 0 }, { 1 }, { 0 }, { 0 }
            },
            {
                { 2 }, { 1, 7, 5 }, { 9 }, { 0 }, { 0 }, { 0 }, { 4 }, { 0 }, { 7 }
            },
            {
                { 0 }, { 6 }, { 1, 7, 5 }, { 5 }, { 0 }, { 1 }, { 0 }, { 7 }, { 0 }
            },
            {
                { 0 }, { 1, 7, 5 }, { 1 }, { 0 }, { 7 }, { 0 }, { 0 }, { 0 }, { 0 }
            },
            {
                { 0 }, { 5 }, { 0 }, { 9 }, { 0 }, { 6 }, { 0 }, { 0 }, { 0 }
            },
            {
                { 5 }, { 1, 2, 3, 4, 7 }, { 0 }, { 0 }, { 1 }, { 0 }, { 0 }, { 0 }, { 6 }
            },
            {
                { 6 }, { 1, 2, 3, 4, 7 }, { 0 }, { 0 }, { 5 }, { 0 }, { 3 }, { 0 }, { 1 }
            },
            {
                { 0 }, { 7 }, { 0 }, { 8 }, { 0 }, { 0 }, { 0 }, { 9 }, { 0 }
            }
    };

}
