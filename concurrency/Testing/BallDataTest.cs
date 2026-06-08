using Data;

namespace Testing
{
    public class BallDataTest
    {

        [Test]
        public void GetDataTest()
        {
            IBallData ball = new BallData();
            ball.Weight = 0.5;
            ball.VelocityX = 1;
            ball.VelocityY = 2;

            Assert.That(ball.Weight, Is.EqualTo(0.5));
            Assert.That(ball.VelocityX, Is.EqualTo(1));
            Assert.That(ball.VelocityY, Is.EqualTo(2));
        }
    }
}