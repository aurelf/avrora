int fib() {
	int a = 1, b = 2, cntr;
	for ( cntr = 0; cntr < 20000; cntr++ ) {
		int c = a + b;
		a = b;
		b = c;
	}

}

