import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HW2 {

    public static void main(String[] args) throws NumberFormatException, IOException {
        File matrixFileA = new File("./src/matrixA.dat");
        File matrixFileB = new File("./src/matrixB.dat");
        SparseMatrixBuilder smatBuilder = new SparseMatrixBuilder();
        SparseMatrix matrixA = smatBuilder.createMatrixFile(matrixFileA);
        SparseMatrix tMatrixA = matrixA.transpose();
        SparseMatrix matrixB = smatBuilder.createMatrixFile(matrixFileB);
        SparseMatrix tMatrixB = matrixB.transpose();
        SparseMatrix product = matrixA.product(matrixB);
        System.out.println("MATRIX A\n");
        matrixA.print();
        System.out.println("\nMATRIX B\n");
        matrixB.print();
        System.out.println("\nMATRIX A TRANSPOSED\n");
        tMatrixA.print();
        System.out.println("\nMatrix B TRANSPOSED\n");
        tMatrixB.print();
        System.out.println("\nMatrix A x Matrix B\n");
        product.print();
    }
}

class SparseMatrixBuilder {
    public SparseMatrixBuilder() {}

    protected SparseMatrix createMatrixFile(File file) throws NumberFormatException, IOException {
        BufferedReader input = new BufferedReader(new FileReader(file));
        SparseMatrix smat = new SparseMatrix();
        String currentLine = "";
        int lineNumber = 1;
        int numOfRows = 0;

        for(int numOfColumns = 0; (currentLine = input.readLine()) != null; ++lineNumber) {
            if(lineNumber == 1) {
                numOfRows = Integer.parseInt(currentLine);
                smat.setNumOfRows(numOfRows);
            } else if(lineNumber == 2) {
                numOfColumns = Integer.parseInt(currentLine);
                smat.setNumOfColumns(numOfColumns);
            } else {
                int row = lineNumber - 2;
                if(!smat.headNodesExist) {
                    smat.createHeadNodes(numOfRows, numOfColumns);
                }

                String[] arrayOfPairStringsInCurrentLine = currentLine.split(" ");
                String[] var10 = arrayOfPairStringsInCurrentLine;
                int var11 = arrayOfPairStringsInCurrentLine.length;

                for(int var12 = 0; var12 < var11; ++var12) {
                    String eachPairString = var10[var12];
                    String[] columnAndValue = eachPairString.split(",");
                    int column = Integer.parseInt(columnAndValue[0]);
                    int value = Integer.parseInt(columnAndValue[1]);
                    smat.insert(row, column, value);
                }
            }
        }
        return smat;
    }
}

class SparseMatrix extends Node {
    private int numOfRows;
    private int numOfColumns;
    protected boolean headNodesExist = false;

    protected void setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
    }

    protected void setNumOfColumns(int numOfColumns) {
        this.numOfColumns = numOfColumns;
    }

    protected SparseMatrix() {}

    protected SparseMatrix(int numOfRows, int numOfColumns) {
        this.headNodesExist = true;
        this.numOfRows = numOfRows;
        this.numOfColumns = numOfColumns;
        this.createHeadNodes(numOfRows, numOfColumns);
    }

    protected SparseMatrix product(SparseMatrix matrixToTranspose) {
        SparseMatrix matrixB = matrixToTranspose.transpose();
        SparseMatrix productMatrix = new SparseMatrix(this.numOfRows, matrixB.numOfRows);

        for(int i = 1; i <= this.numOfRows; ++i) {
            for(int j = 1; j <= matrixB.numOfRows; ++j) {
                int total = 0;
                for(int k = 1; k <= matrixB.numOfColumns; ++k) {
                    int element1 = this.get(i, k);
                    int element2 = matrixB.get(j, k);
                    int temp = element1 * element2;
                    total += temp;
                }
                productMatrix.insert(i, j, total);
            }
        }
        return productMatrix;
    }

    protected void print() {
        for(int i = 1; i <= this.numOfRows; ++i) {
            for(int j = 1; j <= this.numOfColumns; ++j) {
                System.out.print(this.get(i, j) + "\t");
            }
            System.out.println("\n");
        }

    }

    protected SparseMatrix transpose() {
        SparseMatrix tMatrix = new SparseMatrix(this.numOfColumns, this.numOfRows);

        for(int i = 1; i <= this.numOfColumns; ++i) {
            for(int j = 1; j <= this.numOfRows; ++j) {
                int value = this.get(j, i);
                if(value != 0) {
                    tMatrix.insert(i, j, value);
                }
            }
            System.out.println();
        }
        return tMatrix;
    }

    protected void createHeadNodes(int numOfRows, int numOfColumns) {
        this.headNodesExist = true;

        int i;
        for(i = 0; i < numOfRows; ++i) {
            this.addToLastInColumn(this);
        }
        for(i = 0; i < numOfColumns; ++i) {
            this.addToLastInRow(this);
        }
    }

    protected void insert(int row, int column, int value) {
        ValueNode vNode = new ValueNode(row, column, value);
        rowHeadNode rowHeadNode = (rowHeadNode)this.getRow(row);
        rowHeadNode.insert(column, vNode);
        columnHeadNode columnHeadNode = (columnHeadNode)this.getColumn(column);
        columnHeadNode.insert(row, vNode);
    }

    private Node addToLastInRow(Node node) {
        Node head = node;
        if(node == null) {
            return new columnHeadNode();
        } else {
            while(node.getNextInRow() != null) {
                node = node.getNextInRow();
            }

            node.setNextInRow(new columnHeadNode());
            return head;
        }
    }

    private Node addToLastInColumn(Node node) {
        Node head = node;
        if(node == null) {
            return new rowHeadNode();
        } else {
            while(node.getNextInColumn() != null) {
                node = node.getNextInColumn();
            }
            node.setNextInColumn(new rowHeadNode());
            return head;
        }
    }

    private HeadNode getRow(int position) {
        Object current = this;

        for(int i = 0; i < position; ++i) {
            current = ((Node)current).getNextInColumn();
        }
        return (HeadNode)current;
    }

    private HeadNode getColumn(int position) {
        Object current = this;

        for(int i = 0; i < position; ++i) {
            current = ((Node)current).getNextInRow();
        }
        return (HeadNode)current;
    }

    private int get(int row, int column) {
        rowHeadNode h = (rowHeadNode)this.getRow(row);
        ValueNode v = h.get(column);
        return v == null?0:v.getValue();
    }
}

class rowHeadNode extends HeadNode {
    public rowHeadNode() {
    }

    protected void insert(int position, ValueNode vNode) {
        this.appendToRowTail(vNode);
    }

    protected rowHeadNode getNext() {
        return (rowHeadNode)this.getNextInColumn();
    }

    protected ValueNode getFirst() {
        return (ValueNode)this.getNextInRow();
    }

    protected ValueNode get(int position) {
        ValueNode current = this.getFirst();

        do {
            if(position == current.getColumn()) {
                return current;
            }

            if(current.getColumn() > position) {
                return null;
            }

            if(current.getNextInRow() == current) {
                return null;
            }

            current = (ValueNode)current.getNextInRow();
        } while(current != null);

        return null;
    }

    private void appendToRowTail(ValueNode vNode) {
        if(this.getNextInRow() == null) {
            this.setNextInRow(vNode);
        } else {
            ValueNode current;
            for(current = (ValueNode)this.getNextInRow(); current.getNextInRow() != null; current = (ValueNode)current.getNextInRow()) {
                ;
            }

            current.setNextInRow(vNode);
        }

    }
}

class columnHeadNode extends HeadNode {
    public columnHeadNode() {}

    void insert(int position, ValueNode vNode) {
        this.appendToColumnTail(vNode);
    }

    columnHeadNode getNext() {
        return (columnHeadNode)this.getNextInRow();
    }

    ValueNode getFirst() {
        return (ValueNode)this.getNextInColumn();
    }

    public ValueNode get(int position) {
        ValueNode current = this.getFirst();

        do {
            if(position == current.getRow()) {
                return current;
            }

            if(current.getRow() > position) {
                return null;
            }

            if(current.getNextInColumn() == current) {
                return null;
            }

            current = (ValueNode)current.getNextInColumn();
        } while(current != null);

        return null;
    }

    private void appendToColumnTail(ValueNode vNode) {
        if(this.getNextInColumn() == null) {
            this.setNextInColumn(vNode);
        } else {
            ValueNode current;
            for(current = (ValueNode)this.getNextInColumn(); current.getNextInColumn() != null; current = (ValueNode)current.getNextInColumn()) {
                ;
            }

            current.setNextInColumn(vNode);
        }

    }
}

class ValueNode extends Node {
    private int row;
    private int column;
    private int value;

    protected ValueNode(int row, int column, int value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    protected int getRow() {
        return this.row;
    }

    protected void setRow(int row) {
        this.row = row;
    }

    protected int getColumn() {
        return this.column;
    }

    protected void setColumn(int column) {
        this.column = column;
    }

    protected int getValue() {
        return this.value;
    }

    protected void setValue(int value) {
        this.value = value;
    }
}

abstract class HeadNode extends Node {
    public HeadNode() {
    }

    abstract void insert(int var1, ValueNode var2);

    abstract <T extends HeadNode> T getNext();

    abstract <T extends Node> T getFirst();

    abstract <T extends Node> T get(int var1);
}

abstract class Node {
    private Node nextInRow;
    private Node nextInColumn;

    public Node() {
    }

    public Node getNextInRow() {
        return this.nextInRow;
    }

    public void setNextInRow(Node nextInRow) {
        this.nextInRow = nextInRow;
    }

    public Node getNextInColumn() {
        return this.nextInColumn;
    }

    public void setNextInColumn(Node nextInColumn) {
        this.nextInColumn = nextInColumn;
    }
}
