using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Text;

namespace Model
{
    public class ModelRepo : INotifyPropertyChanged
    {
        private ObservableCollection<BallModel> _balls = new ObservableCollection<BallModel>();
        private string _ballsNumber;

        public ObservableCollection<BallModel> Balls
        {
            get { return _balls; }
            set
            {
                _balls = value;
                OnPropertyChanged();
            }
        }

        public string BallsNumber
        {
            get { return _ballsNumber; }
            set
            {
                _ballsNumber = value;
                OnPropertyChanged();
            }
        }
        
        

        public event PropertyChangedEventHandler PropertyChanged;

        private void OnPropertyChanged([CallerMemberName] string propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
