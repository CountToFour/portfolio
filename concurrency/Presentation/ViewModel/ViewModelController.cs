using Logic;
using Model;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;

namespace ViewModel
{
    public class ViewModelController : INotifyPropertyChanged
    {
        private BallLogic _ballLogic = new BallLogic();
        private ModelRepo _repo;

        public ICommand CreateBallCommand { get; set; }

        public ViewModelController()
        {
            CreateBallCommand = new RelayCommand(CreateBalls);
            Repo = _ballLogic.RepoModel;
            Repo.BallsNumber = "0";
        }

        private void CreateBalls(object obj)
        {
            _ballLogic.CreateBalls();
        }

        public ModelRepo Repo
        {
            get { return _repo; }
            set
            {
                _repo = value;
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
