int method();

int main() {
  int cntr;
  int array[32];

  for ( cntr = 0; cntr < 32; cntr++ ) {
    array[cntr] = method();
  }


}
