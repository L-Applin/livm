package ca.applin.livm;

public record Word(Integer word) {
    public static final Word WORD_0 = new Word(0);
    public static final Word WORD_1 = new Word(1);

    @Override
    public String toString() {
        return "Word[" + word + ']';
    }
}

