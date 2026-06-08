using System.ComponentModel;
using System.Runtime.CompilerServices;


namespace Model
{
    public class BallModel : INotifyPropertyChanged
    {
        private double _posX;
        private double _posY;

        public double PosX
        {
            get { return _posX; }
            set
            {
                _posX = value;
                OnPropertyChanged();
            }
        }

        public double PosY
        {
            get { return _posY; }
            set
            {
                _posY = value;
                OnPropertyChanged();
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        private void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
