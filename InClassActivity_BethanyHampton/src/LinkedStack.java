public class StackExameple<E> {
    private Node<E> last;
    private int count;

    public LinkedStack() {
        last = null;
        count = 0;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void push(E element) {
        if (size() == 0) {
            last = new Node<E>(null, element);
        } else {
            last = new Node<E>(last, element);
        }

        count++;
    }

    public E pop() {
        if (size() == 0) {
            return null;
        }

        Node<E> curr = last;
        last = last.pre;
        curr.pre = null;
        count--;

        return curr.element;
    }

    public E top() {
        if (size() == 0) {
            return null;
        }

        return last.element;
    }

    private static class Node<E> {
        Node<E> pre;
        E element;

        Node(Node<E> pre, E element) {
            this.pre = pre;
            this.element = element;
        }

        public void showStack() {
            Node<E> current = last;
            while (current != null) {
                System.out.print(current.element + " ");
                current = current.pre;
                System.out.println();
            }
        }
    }
}