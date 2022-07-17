import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;


/*
An office has a bathroom that can be used by both men and women, but not both
at the same time. If a man is in the bathroom, other men may enter, but any
women wishing to use the bathroom should wait for it to be empty. If a woman
is in the bathroom, other women may enter, but any men wishing to use the
bathroom should wait it to be empty. Each person (man or woman) will spend
some time using the bathroom.
*/

enum Gender {
	MALE, FEMALE;
}

class Bathroom {
	private final int MAX_OCCUPANCY;
	private final Semaphore semaphore;
	private volatile Gender currentGender;
	private final ReentrantLock reentrantLock;
	private final Condition condition;
	private final Long duration = 3000L;

	Bathroom(final int maxOccupancy) {
		MAX_OCCUPANCY = maxOccupancy;
		semaphore = new Semaphore(maxOccupancy);
		reentrantLock = new ReentrantLock();
		condition = reentrantLock.newCondition();
	}

	public void acquire(final Gender gender) {
		final String name = Thread.currentThread().getName();
		try {
			reentrantLock.lock();
			while (true) {
				if (semaphore.availablePermits() == MAX_OCCUPANCY) currentGender = gender;
				if (gender != currentGender) condition.await();
				else break;
			}
			reentrantLock.unlock();
			semaphore.acquire();
			System.out.printf("%s (%s) using bathroom.. \n", name, gender);
			Thread.sleep(duration);
			semaphore.release();
			System.out.println();
			reentrantLock.lock();
			condition.signalAll();
			reentrantLock.unlock();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}

class Person extends Thread {

	private final Gender gender;
	private final Bathroom bathroom;

	public Person (final Gender gender, final Bathroom bathroom) {
		if (gender == null || bathroom == null) throw new IllegalArgumentException("Null args supplied");
		this.gender = gender;
		this.bathroom = bathroom;
	}

	public void run() {
		bathroom.acquire(this.gender);
	}
}

public class UnisexBathroom {
	public static void main(String... args) {

		final Random random = new Random();
		final Bathroom bathroom = new Bathroom(3);

		for (int i=0; i<100; i++) {
			final Gender gender = (random.nextInt(100)&1) == 0 ? Gender.MALE : Gender.FEMALE;
			new Person(gender, bathroom).start();
		}
	}
}