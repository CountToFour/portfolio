namespace Data
{
    public interface IBallData
    {
        public double Weight { get; set; }
        public double VelocityX { get; set; }
        public double VelocityY { get; set; }
    }

    public class BallData : IBallData
    {
        private double _weight;
        private double _velocityX;
        private double _velocityY;

        public double Weight
        {
            get { return _weight; }
            set
            {
                _weight = value;
            }
        }

        public double VelocityX
        {
            get { return _velocityX; }
            set
            {
                _velocityX = value;
            }
        }
        
        public double VelocityY
        {
            get { return _velocityY; }
            set
            {
                _velocityY = value;
            }
        }
    }
}
