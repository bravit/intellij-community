// "Replace 'switch' with 'if'" "true-preview"
abstract class Test {
  abstract Object getObject();

  void foo() {
      Class<?> aClass = getObject().getClass();
      if (RuntimeException.class.equals(aClass)) {
      } else if (IOException.class.equals(aClass)) {
      }
  }
}